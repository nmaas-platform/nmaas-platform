package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.repository.DcnInfo;
import net.geant.nmaas.dcn.deployment.repository.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.*;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DcnDeploymentCoordinator implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private final static Logger log = LogManager.getLogger(DcnDeploymentCoordinator.class);

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DockerHostRepository dockerHostRepository;

    private DcnRepository dcnRepository;

    private DeploymentIdToDcnNameMapper deploymentIdMapper;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public DcnDeploymentCoordinator(DockerHostRepository dockerHostRepository,
                                    DcnRepository dcnRepository,
                                    DeploymentIdToDcnNameMapper deploymentIdMapper,
                                    ApplicationEventPublisher applicationEventPublisher) {
        this.dockerHostRepository = dockerHostRepository;
        this.dcnRepository = dcnRepository;
        this.deploymentIdMapper = deploymentIdMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec) {
        final String dcnName = dcnSpec.name();
        deploymentIdMapper.storeMapping(deploymentId, dcnName);
        dcnRepository.storeNetwork(new DcnInfo(dcnName, DcnDeploymentState.INIT, dcnSpec));
        try {
            dcnRepository.updateAnsiblePlaybookForClientSideRouter(dcnName, AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter());
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig = AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter();
            cloudSideRouterVpnConfig.merge((ContainerNetworkDetails) dcnSpec.getNmServiceDeploymentNetworkDetails());
            dcnRepository.updateAnsiblePlaybookForCloudSideRouter(dcnName, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch (DcnRepository.DcnNotFoundException e) {
            log.error("Exception during DCN request verification -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN environment preparation functionality (currently not required)
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.ENVIRONMENT_PREPARED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        String dcnName = null;
        try {
            dcnName = deploymentIdMapper.dcnName(deploymentId);
            final DcnInfo dcnInfo = dcnRepository.loadNetwork(dcnName);
            final DockerHost ansibleContainerDockerHost = loadDefaultAnsibleDockerHost();
            deployAnsiblePlaybookContainers(ansibleContainerDockerHost,
                    buildContainerForClientSideRouterConfig(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(dcnName)),
                    buildContainerForCloudSideRouterConfig(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(dcnName)));
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch (DeploymentIdToDcnNameMapper.EntryNotFoundException deploymentIdNotFoundException) {
            throw new InvalidDeploymentIdException();
        } catch (DcnRepository.DcnNotFoundException
                | DockerHostNotFoundException
                | InterruptedException
                | DockerException anyException) {
            log.error("Exception during DCN deployment -> " + anyException.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.DEPLOYMENT_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN verification functionality
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        String dcnName = null;
        try {
            dcnName = deploymentIdMapper.dcnName(deploymentId);
            final DcnInfo dcnInfo = dcnRepository.loadNetwork(dcnName);
            final DockerHost ansibleContainerDockerHost = loadDefaultAnsibleDockerHost();
            deployAnsiblePlaybookContainers(ansibleContainerDockerHost,
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(dcnName)),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(dcnName)));
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REMOVAL_INITIATED);
        } catch (DeploymentIdToDcnNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (DcnRepository.DcnNotFoundException
                 | DockerHostNotFoundException
                 | InterruptedException
                 | DockerException e) {
            log.error("Exception during DCN removal -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REMOVAL_FAILED);
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, state));
    }

    private void deployAnsiblePlaybookContainers(DockerHost dockerHost, ContainerConfig... ansibleContainerConfigs) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        for (ContainerConfig containerConfig : ansibleContainerConfigs) {
            ContainerCreation ansibleContainer = apiClient.createContainer(containerConfig, ansibleContainerName());
            apiClient.startContainer(ansibleContainer.id());
        }
    }

    private DockerHost loadDefaultAnsibleDockerHost() throws DockerHostNotFoundException {
        return dockerHostRepository.loadByName(DockerHostRepository.ANSIBLE_DOCKER_HOST_NAME);
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyPlaybookExecutionState(String encodedPlaybookIdentifier, AnsiblePlaybookStatus.Status status) {
        Identifier deploymentId = null;
        try {
            final String dcnName = decode(encodedPlaybookIdentifier);
            deploymentId = deploymentIdMapper.deploymentId(dcnName);
            DcnDeploymentState currentDcnDeploymentState = dcnRepository.loadCurrentState(dcnName);
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
             notifyStateChangeListeners(deploymentId, newDcnDeploymentState);
        } catch (DcnRepository.DcnNotFoundException
                | DeploymentIdToDcnNameMapper.EntryNotFoundException
                | AnsiblePlaybookIdentifierConverterException e) {
            log.error("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.ERROR);
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
