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
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

@Component
public class DcnDeploymentCoordinator implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DockerHostRepository dockerHostRepository;

    private DcnRepository dcnRepository;

    private DeploymentIdToDcnNameMapper deploymentIdMapper;

    AppDeploymentStateChangeListener defaultAppDeploymentStateChangeListener;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Autowired
    public DcnDeploymentCoordinator(DockerHostRepository dockerHostRepository,
                                    DcnRepository dcnRepository,
                                    DeploymentIdToDcnNameMapper deploymentIdMapper,
                                    AppDeploymentStateChangeListener defaultAppDeploymentStateChangeListener) {
        this.dockerHostRepository = dockerHostRepository;
        this.dcnRepository = dcnRepository;
        this.deploymentIdMapper = deploymentIdMapper;
        this.defaultAppDeploymentStateChangeListener = defaultAppDeploymentStateChangeListener;
    }

    @Override
    public void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec) {
        final String dcnName = dcnSpec.name();
        deploymentIdMapper.storeMapping(deploymentId, dcnName);
        dcnRepository.storeNetwork(new DcnInfo(dcnName, DcnDeploymentState.INIT, dcnSpec));
        try {
            dcnRepository.updateAnsiblePlaybookForClientSideRouter(dcnName, AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter());
            dcnRepository.updateAnsiblePlaybookForCloudSideRouter(dcnName, AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch (DcnRepository.DcnNotFoundException e) {
            System.out.println("Exception during DCN request verification -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
        }
    }

    @Override
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN environment preparation functionality (currently not required)
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.ENVIRONMENT_PREPARED);
    }

    @Override
    public void deployDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        try {
            String dcnName = deploymentIdMapper.dcnName(deploymentId);
            final AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter = dcnRepository.loadNetwork(dcnName).getAnsiblePlaybookForClientSideRouter();
            final AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter = dcnRepository.loadNetwork(dcnName).getAnsiblePlaybookForCloudSideRouter();
            final DockerHost ansibleContainerDockerHost = loadDefaultAnsibleDockerHost();
            final ContainerConfig ansiblePlaybookClientSideRouterContainerConfig =
                    AnsibleContainerConfigBuilder.build(ansiblePlaybookForClientSideRouter, encodeForClientSideRouter(dcnName));
            executeAnsiblePlaybookContainerDeploy(ansiblePlaybookClientSideRouterContainerConfig, ansibleContainerName(), ansibleContainerDockerHost);
            final ContainerConfig ansiblePlaybookCloudSideRouterContainerConfig =
                    AnsibleContainerConfigBuilder.build(ansiblePlaybookForCloudSideRouter, encodeForCloudSideRouter(dcnName));
            executeAnsiblePlaybookContainerDeploy(ansiblePlaybookCloudSideRouterContainerConfig, ansibleContainerName(), ansibleContainerDockerHost);
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch (DeploymentIdToDcnNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (DcnRepository.DcnNotFoundException
                | DockerHostNotFoundException
                | InterruptedException
                | DockerException e) {
            System.out.println("Exception during DCN deployment -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.DEPLOYMENT_FAILED);
        }
    }

    @Override
    public void verifyDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN verification functionality
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.VERIFIED);
    }

    @Override
    public void removeDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN removal functionality
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.REMOVED);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, DcnDeploymentState state) {
        defaultAppDeploymentStateChangeListener.notifyStateChange(deploymentId, state);
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

    private void executeAnsiblePlaybookContainerDeploy(ContainerConfig ansibleContainerConfig, String containerName, DockerHost dockerHost) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        ContainerCreation ansibleContainer = apiClient.createContainer(ansibleContainerConfig, containerName);
        apiClient.startContainer(ansibleContainer.id());
    }

    private DockerHost loadDefaultAnsibleDockerHost() throws DockerHostNotFoundException {
        return dockerHostRepository.loadByName(DockerHostRepository.ANSIBLE_DOCKER_HOST_NAME);
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
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
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        default:
                            newDcnDeploymentState = DcnDeploymentState.ERROR;
                    }
                    break;
                case FAILURE:
                default:
                    newDcnDeploymentState = DcnDeploymentState.DEPLOYMENT_FAILED;
            }
            dcnRepository.updateDcnState(dcnName, newDcnDeploymentState);
            notifyStateChangeListeners(deploymentId, newDcnDeploymentState);
        } catch (DcnRepository.DcnNotFoundException
                | DeploymentIdToDcnNameMapper.EntryNotFoundException
                | AnsiblePlaybookIdentifierConverterException e) {
            System.out.println("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.ERROR);
        }
    }
}
