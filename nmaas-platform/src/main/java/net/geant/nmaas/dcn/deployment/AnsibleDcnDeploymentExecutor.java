package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.CustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.*;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

/**
 * Executor used when DCN should be configured by Ansible playbooks.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("dcn_ansible")
public class AnsibleDcnDeploymentExecutor implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private static final Logger log = LogManager.getLogger(AnsibleDcnDeploymentExecutor.class);
    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private DockerHostAttachPointRepository dockerHostAttachPointRepository;
    private BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;
    private DockerHostNetworkRepository dockerHostNetworkRepository;
    private DockerApiClient dockerApiClient;

    private String ansibleDockerApiUrl;

    @Autowired
    public AnsibleDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager,
                                        DockerHostAttachPointRepository dockerHostAttachPointRepository,
                                        BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository,
                                        ApplicationEventPublisher applicationEventPublisher,
                                        DockerHostNetworkRepository dockerHostNetworkRepository,
                                        DockerApiClient dockerApiClient) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.dockerHostAttachPointRepository = dockerHostAttachPointRepository;
        this.basicCustomerNetworkAttachPointRepository = basicCustomerNetworkAttachPointRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dockerHostNetworkRepository = dockerHostNetworkRepository;
        this.dockerApiClient = dockerApiClient;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public DcnState checkState(Identifier clientId) {
        try {
            return DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(clientId));
        } catch (InvalidClientIdException e) {
            return DcnState.NONE;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        try {
            storeDcnInfoIfNotExists(clientId, dcnSpec);
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUESTED);
            final DockerHostNetwork dockerHostNetwork = dockerHostNetworkRepository
                    .findByClientId(dcnSpec.getClientId())
                    .orElseThrow(() -> new InvalidClientIdException("No Docker network found for client " + clientId));
            final DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(dockerHostNetwork);
            dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, dcnCloudEndpointDetails);
            CustomerNetworkAttachPoint customerNetworkAttachPoint = basicCustomerNetworkAttachPointRepository
                   .findByCustomerId(clientId.longValue())
                   .orElseThrow(() -> new AttachPointNotFoundException(clientId.value()));
            AnsiblePlaybookVpnConfig customerSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCustomerNetworkAttachPoint(customerNetworkAttachPoint);
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(
                    clientId,
                    customerSideRouterVpnConfig);
            CloudAttachPoint cloudAttachPoint = dockerHostAttachPointRepository
                    .findByDockerHostName(dockerHostNetwork.getHost().getName())
                    .orElseThrow(() -> new AttachPointNotFoundException(dockerHostNetwork.getHost().getName()));
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCloudAttachPoint(customerSideRouterVpnConfig, cloudAttachPoint);
            cloudSideRouterVpnConfig.merge(dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch ( InvalidClientIdException
                | AttachPointNotFoundException e) {
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    private void storeDcnInfoIfNotExists(Identifier clientId, DcnSpec dcnSpec) throws InvalidClientIdException {
        if (!dcnRepositoryManager.exists(clientId))
            dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(Identifier clientId) throws CouldNotDeployDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(clientId);
            removeOldAnsiblePlaybookContainers();
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfig(
                            dcnInfo.getPlaybookForClientSideRouter(),
                            encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfig(
                            dcnInfo.getPlaybookForCloudSideRouter(),
                            encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch ( InvalidClientIdException
                | InterruptedException
                | DockerException anyException) {
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_FAILED);
            throw new CouldNotDeployDcnException("Exception during DCN deployment -> " + anyException.getMessage());
        }
    }

    private void removeOldAnsiblePlaybookContainers() {
        try {
            final List<Container> containers = dockerApiClient.listContainers(ansibleDockerApiUrl, DockerClient.ListContainersParam.withStatusExited());
            for (Container container : containers) {
                log.debug("Removing old container " + container.id());
                dockerApiClient.removeContainer(ansibleDockerApiUrl, container.id());
            }
        } catch (DockerException
                | InterruptedException e) {
            log.warn("Failed to remove old Ansible containers", e);
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
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getPlaybookForClientSideRouter(), encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getPlaybookForCloudSideRouter(), encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_INITIATED);
        } catch ( InvalidClientIdException
                | InterruptedException
                | DockerException e) {
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_FAILED);
            throw new CouldNotRemoveDcnException("Exception during DCN removal -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier clientId, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, clientId, state));
    }

    private void deployAnsiblePlaybookContainers(ContainerConfig... ansibleContainerConfigs) throws DockerException, InterruptedException {
        for (ContainerConfig containerConfig : ansibleContainerConfigs) {
            String ansibleContainerId = dockerApiClient.createContainer(ansibleDockerApiUrl, containerConfig, ansibleContainerName());
            dockerApiClient.startContainer(ansibleDockerApiUrl, ansibleContainerId);
        }
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

    @Value("${ansible.docker.api.url}")
    public void setAnsibleDockerApiUrl(String ansibleDockerApiUrl) {
        this.ansibleDockerApiUrl = ansibleDockerApiUrl;
    }

}
