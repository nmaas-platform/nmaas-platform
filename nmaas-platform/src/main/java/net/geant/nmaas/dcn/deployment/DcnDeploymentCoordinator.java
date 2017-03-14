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
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.dcn.deployment.DcnDeploymentState.INIT;

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
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec) {
        final String dcnName = dcnSpec.name();
        deploymentIdMapper.storeMapping(deploymentId, dcnName);
        dcnRepository.storeNetwork(new DcnInfo(dcnName, INIT, dcnSpec));
        VpnConfig vpnConfig = null;
        try {
            vpnConfig = VpnConfig.defaultVpn();
            dcnRepository.updateVpnConfig(dcnName, vpnConfig);
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch (DcnRepository.DcnNotFoundException e) {
            System.out.println("Exception during DCN request verification -> " + e.getMessage());
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
            final VpnConfig vpnConfig = dcnRepository.loadNetwork(dcnName).getVpnConfig();
            final String encodedDcnName = DcnIdentifierConverter.encode(dcnName);
            final ContainerConfig ansibleContainerConfig = AnsibleContainerConfigBuilder.build(vpnConfig, encodedDcnName);
            executeDeploy(ansibleContainerConfig, ansibleContainerName(), loadDefaultAnsibleDockerHost());
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
    @Loggable(LogLevel.INFO)
    public void verifyDcn(Identifier deploymentId) throws InvalidDeploymentIdException {
        // TODO implement DCN verification functionality
        notifyStateChangeListeners(deploymentId, DcnDeploymentState.VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
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

    private void executeDeploy(ContainerConfig ansibleContainerConfig, String containerName, DockerHost dockerHost) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        ContainerCreation ansibleContainer = apiClient.createContainer(ansibleContainerConfig, containerName);
        apiClient.startContainer(ansibleContainer.id());
    }

    private void removePreviousContainer() {
    }

    private DockerHost loadDefaultAnsibleDockerHost() throws DockerHostNotFoundException {
        return dockerHostRepository.loadByName(DockerHostRepository.ANSIBLE_DOCKER_HOST_NAME);
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyPlaybookExecutionState(String encodedServiceId, AnsiblePlaybookStatus.Status status) {
        final String dcnName = DcnIdentifierConverter.decode(encodedServiceId);
        DcnDeploymentState dcnDeploymentState;
        switch (status) {
            case SUCCESS:
                dcnDeploymentState = DcnDeploymentState.DEPLOYED;
                break;
            case FAILURE:
            default:
                dcnDeploymentState = DcnDeploymentState.DEPLOYMENT_FAILED;
        }
        Identifier deploymentId = null;
        try {
            dcnRepository.updateDcnState(dcnName, dcnDeploymentState);
            deploymentId = deploymentIdMapper.deploymentId(dcnName);
            notifyStateChangeListeners(deploymentId, dcnDeploymentState);
        } catch (DcnRepository.DcnNotFoundException
                | DeploymentIdToDcnNameMapper.EntryNotFoundException e) {
            System.out.println("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DcnDeploymentState.ERROR);
        }
    }
}
