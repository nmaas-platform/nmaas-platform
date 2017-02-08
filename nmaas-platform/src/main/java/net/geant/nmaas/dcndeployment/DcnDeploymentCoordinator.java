package net.geant.nmaas.dcndeployment;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import net.geant.nmaas.dcndeployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcndeployment.repository.DcnInfo;
import net.geant.nmaas.dcndeployment.repository.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DcnDeploymentCoordinator {

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DockerHostRepository dockerHostRepository;

    private DcnRepository dcnRepository;

    @Autowired
    public DcnDeploymentCoordinator(DockerHostRepository dockerHostRepository, DcnRepository dcnRepository) {
        this.dockerHostRepository = dockerHostRepository;
        this.dcnRepository = dcnRepository;
    }

    public void deploy(String dcnName, VpnConfig vpn) throws DockerHostNotFoundException {
        storeInRepository(dcnName, vpn);
        final String serviceId = DcnIdentifierConverter.encode(dcnName);
        ContainerConfig ansibleContainerConfig = AnsibleContainerConfigBuilder.build(vpn, serviceId);
        executeDeploy(ansibleContainerConfig, ansibleContainerName(), loadDefaultAnsibleDockerHost());
    }

    private void executeDeploy(ContainerConfig ansibleContainerConfig, String containerName, DockerHost dockerHost) {
        DockerClient apiClient = DefaultDockerClient.builder().uri(dockerHost.apiUrl()).build();
        try {
            ContainerCreation ansibleContainer = apiClient.createContainer(ansibleContainerConfig, containerName);
            apiClient.startContainer(ansibleContainer.id());
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removePreviousContainer() {
    }

    private void storeInRepository(String dcnName, VpnConfig vpn) {
        final DcnInfo dcnInfo = new DcnInfo(dcnName);
        dcnInfo.setVpnConfig(vpn);
        dcnRepository.storeNetwork(dcnInfo);
    }

    private DockerHost loadDefaultAnsibleDockerHost() throws DockerHostNotFoundException {
        return dockerHostRepository.loadByName(DockerHostRepository.ANSIBLE_DOCKER_HOST_NAME);
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    public void notifyPlaybookExecutionState(String encodedServiceId, AnsiblePlaybookStatus.Status status) {
        final String dcnName = DcnIdentifierConverter.decode(encodedServiceId);
        DcnDeploymentState dcnDeploymentState;
        switch (status) {
            case SUCCESS:
                dcnDeploymentState = DcnDeploymentState.CONFIGURED;
                break;
            case FAILURE:
            default:
                dcnDeploymentState = DcnDeploymentState.ERROR;
        }
        try {
            dcnRepository.updateDcnState(dcnName, dcnDeploymentState);
        } catch (DcnRepository.DcnNotFoundException e) {
            e.printStackTrace();
        }
    }
}
