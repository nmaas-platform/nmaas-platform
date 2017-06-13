package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerContainerManager {

    private DockerApiClient dockerApiClient;

    @Autowired
    public DockerContainerManager(DockerApiClient dockerApiClient) {
        this.dockerApiClient = dockerApiClient;
    }

    public DockerContainer declareNewContainerForDeployment(Identifier deploymentId) {
        final DockerContainer dockerContainer = new DockerContainer();
        dockerContainer.setVolumesDetails(new DockerContainerVolumesDetails(ContainerConfigBuilder.getPrimaryVolumeName(deploymentId.value())));
        return dockerContainer;
    }

    public String create(ContainerConfig containerConfig, String name, DockerHost host)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            return executeCreate(containerConfig, name, host.apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private String executeCreate(ContainerConfig containerConfig, String name, String apiUrl)
            throws DockerException, InterruptedException {
        return dockerApiClient.createContainer(apiUrl, containerConfig, name);
    }

    public void start(String containerId, DockerHost host)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            executeStart(containerId, host.apiUrl());
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private void executeStart(String containerId, String apiUrl)
            throws DockerException, InterruptedException {
        dockerApiClient.startContainer(apiUrl, containerId);
    }

    public void addStaticRoute(String containerId, String deviceAddress, String gatewayAddress, DockerHost host)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            executeExec(containerId, addIpRouteCommand(deviceAddress, gatewayAddress), host.apiUrl());
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not exec route add on container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private List<String> addIpRouteCommand(String deviceAddress, String gatewayAddress) {
        List<String> commands = new ArrayList<>();
        commands.add("ip");
        commands.add("route");
        commands.add("add");
        commands.add(deviceAddress + "/32");
        commands.add("via");
        commands.add(gatewayAddress);
        return commands;
    }

    private void executeExec(String containerId, List<String> commands, String apiUrl) throws DockerException, InterruptedException {
        final ExecCreation execCreation = dockerApiClient.execCreate(apiUrl, containerId, commands.stream().toArray(String[]::new), DockerClient.ExecCreateParam.privileged(true));
        final String execId = execCreation.id();
        dockerApiClient.execStart(apiUrl, execId);
    }

    public void remove(String containerId, DockerHost host)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            executeStopAndRemove(containerId, host.apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotRemoveNmServiceException(
                    "Could not remove container " + containerId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private void executeStopAndRemove(String containerId, String apiUrl)
            throws DockerException, InterruptedException {
        if (checkIfContainerRunning(containerId, apiUrl)) {
            dockerApiClient.stopContainer(apiUrl,containerId, 3);
            dockerApiClient.removeContainer(apiUrl, containerId);
        }
    }

    private boolean checkIfContainerRunning(String containerId, String apiUrl)
            throws DockerException, InterruptedException {
        return dockerApiClient.listContainers(apiUrl, DockerClient.ListContainersParam.withStatusRunning()).stream().anyMatch(c -> c.id().equals(containerId));
    }

    public void pullImage(String imageName, DockerHost host)
            throws ContainerOrchestratorInternalErrorException {
        try {
            executePullImage(imageName, host.apiUrl());
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not pull image " + imageName + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private void executePullImage(String imageName, String apiUrl)
            throws DockerException, InterruptedException {
        dockerApiClient.pull(apiUrl, imageName);
    }

    public List<String> containers(DockerHost host)
            throws ContainerOrchestratorInternalErrorException {
        try {
            return executeListContainersAndReturnIds(host.apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private List<String> executeListContainersAndReturnIds(String apiUrl) throws DockerException, InterruptedException {
        List<Container> containers = dockerApiClient.listContainers(apiUrl, DockerClient.ListContainersParam.allContainers());
        return containers.stream().map((container -> container.id())).collect(Collectors.toList());
    }

    public void checkService(String containerId, DockerHost host)
            throws ContainerCheckFailedException, ContainerOrchestratorInternalErrorException {
        try {
            if (!NmServiceDeploymentState.DEPLOYED.equals(executeInspectContainerAndReturnContainerState(containerId, host.apiUrl())))
                throw new ContainerCheckFailedException("Container with id " + containerId + " is stopped");
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not check service state -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    private NmServiceDeploymentState executeInspectContainerAndReturnContainerState(String containerId, String apiUrl)
            throws DockerException, InterruptedException {
        ContainerInfo containerInfo = dockerApiClient.inspectContainer(apiUrl, containerId);
        return serviceStateFromContainerInfo(containerInfo);
    }

    private NmServiceDeploymentState serviceStateFromContainerInfo(ContainerInfo containerInfo) {
        switch(containerInfo.state().status()) {
            case "exited":
                return NmServiceDeploymentState.REMOVED;
            case "running":
                return NmServiceDeploymentState.DEPLOYED;
            default:
                return NmServiceDeploymentState.ERROR;
        }
    }

}
