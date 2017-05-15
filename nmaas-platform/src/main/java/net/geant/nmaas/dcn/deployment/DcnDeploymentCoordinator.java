package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigRepository;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories.DockerNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.*;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DcnDeploymentCoordinator implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private final static Logger log = LogManager.getLogger(DcnDeploymentCoordinator.class);

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private DcnRepositoryManager dcnRepositoryManager;

    private ApplicationEventPublisher applicationEventPublisher;

    private AnsiblePlaybookVpnConfigRepository vpnConfigRepository;

    private DockerNetworkRepository dockerNetworkRepository;

    @Autowired
    public DcnDeploymentCoordinator(DockerHostRepositoryManager dockerHostRepositoryManager,
                                    DcnRepositoryManager dcnRepositoryManager,
                                    AnsiblePlaybookVpnConfigRepository vpnConfigRepository,
                                    ApplicationEventPublisher applicationEventPublisher,
                                    DockerNetworkRepository dockerNetworkRepository) {
        this.dockerHostRepositoryManager = dockerHostRepositoryManager;
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.vpnConfigRepository = vpnConfigRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dockerNetworkRepository = dockerNetworkRepository;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public boolean checkIfExists(Identifier clientId) {
        try {
            dcnRepositoryManager.loadCurrentState(clientId);
            return true;
        } catch (InvalidClientIdException e) {
            return false;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
        try {
            final DockerNetwork dockerNetwork = dockerNetworkRepository.findByClientId(dcnSpec.getClientId()).orElseThrow(() -> new InvalidClientIdException());
            final DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(dockerNetwork);
            dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(clientId, vpnConfigRepository.loadDefaultCustomerVpnConfig());
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig = vpnConfigRepository.loadDefaultCloudVpnConfig();
            cloudSideRouterVpnConfig.merge(dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch ( InvalidClientIdException
                | AnsiblePlaybookVpnConfigNotFoundException e) {
            log.error("Exception during DCN request verification -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(Identifier clientId) throws CouldNotDeployDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(clientId);
            final DockerHost ansibleContainerDockerHost = loadDefaultAnsibleDockerHost();
            removeOldAnsiblePlaybookContainers(ansibleContainerDockerHost);
            deployAnsiblePlaybookContainers(ansibleContainerDockerHost,
                    buildContainerForClientSideRouterConfig(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfig(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch ( InvalidClientIdException
                | DockerHostNotFoundException
                | DockerHostInvalidException
                | InterruptedException
                | DockerException anyException) {
            log.error("Exception during DCN deployment -> " + anyException.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_FAILED);
            throw new CouldNotDeployDcnException("Exception during DCN deployment -> " + anyException.getMessage());
        }
    }

    void removeOldAnsiblePlaybookContainers(DockerHost dockerHost) {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        try {
            final List<Container> containers = apiClient.listContainers(DockerClient.ListContainersParam.withStatusExited());
            for (Container container : containers) {
                log.debug("Removing old container " + container.id());
                apiClient.removeContainer(container.id());
            }
        } catch (DockerException
                | InterruptedException e) {
            log.warn("Failed to removeIfNoContainersAttached old Ansible containers", e);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(Identifier clientId) throws CouldNotVerifyDcnException {
        try {
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFICATION_INITIATED);
            // TODO implement DCN verification functionality
            Thread.sleep(1000);
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFIED);
        } catch (InterruptedException e) {
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFICATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(Identifier clientId) throws CouldNotRemoveDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(clientId);
            final DockerHost ansibleContainerDockerHost = loadDefaultAnsibleDockerHost();
            deployAnsiblePlaybookContainers(ansibleContainerDockerHost,
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_INITIATED);
        } catch ( InvalidClientIdException
                | DockerHostNotFoundException
                | DockerHostInvalidException
                | InterruptedException
                | DockerException e) {
            log.error("Exception during DCN removal -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_FAILED);
            throw new CouldNotRemoveDcnException("Exception during DCN removal -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier clientId, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, clientId, state));
    }

    private void deployAnsiblePlaybookContainers(DockerHost dockerHost, ContainerConfig... ansibleContainerConfigs) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        for (ContainerConfig containerConfig : ansibleContainerConfigs) {
            ContainerCreation ansibleContainer = apiClient.createContainer(containerConfig, ansibleContainerName());
            apiClient.startContainer(ansibleContainer.id());
        }
    }

    private DockerHost loadDefaultAnsibleDockerHost() throws DockerHostNotFoundException, DockerHostInvalidException {
        return dockerHostRepositoryManager.loadByName(DockerHostRepositoryManager.ANSIBLE_DOCKER_HOST_NAME);
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyPlaybookExecutionState(String encodedPlaybookIdentifier, AnsiblePlaybookStatus.Status status) {
        Identifier clientId = null;
        try {
            clientId = Identifier.newInstance(decode(encodedPlaybookIdentifier));
            DcnDeploymentState currentDcnDeploymentState = dcnRepositoryManager.loadCurrentState(clientId);
            DcnDeploymentState newDcnDeploymentState = null;
            switch (status) {
                case SUCCESS:
                    switch(currentDcnDeploymentState) {
                        case DEPLOYMENT_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case REMOVAL_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        default:
                            newDcnDeploymentState = DcnDeploymentState.ERROR;
                    }
                    break;
                case FAILURE:
                default:
                    newDcnDeploymentState = deploymentOrRemovalFailureDependingOnLastState(currentDcnDeploymentState);
            }
             notifyStateChangeListeners(clientId, newDcnDeploymentState);
        } catch ( InvalidClientIdException
                | AnsiblePlaybookIdentifierConverterException e) {
            log.error("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.ERROR);
        }
    }

    DcnDeploymentState deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState currentDcnDeploymentState) {
        switch (currentDcnDeploymentState) {
            case DEPLOYMENT_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.DEPLOYMENT_FAILED;
            case REMOVAL_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.REMOVAL_FAILED;
            default:
                return DcnDeploymentState.ERROR;
        }
    }

}
