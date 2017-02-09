package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClientFactory;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerNotFoundException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDestroyNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerContainerClient {

    public String deploy(ContainerConfig containerConfig, String name, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            return executeDeploy(containerConfig, name, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private String executeDeploy(ContainerConfig containerConfig, String name, DockerClient apiClient)
            throws DockerException, InterruptedException {
        final ContainerCreation container = apiClient.createContainer(containerConfig, name);
        final String containerId = container.id();
        apiClient.startContainer(containerId);
        return containerId;
    }

    public void remove(String containerId, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            executeStopAndRemove(containerId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException(
                    "Could not remove container " + containerId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private void executeStopAndRemove(String containerId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        apiClient.stopContainer(containerId, 3);
        apiClient.removeContainer(containerId);
    }

    public void pullImage(String imageName, DockerHost host)
            throws ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            executePullImage(imageName, apiClient);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not pull image " + imageName + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private void executePullImage(String imageName, DockerClient apiClient)
            throws DockerException, InterruptedException {
        apiClient.pull(imageName);
    }

    public List<String> containers(DockerHost host)
            throws CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            return executeListContainersAndReturnIds(apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private List<String> executeListContainersAndReturnIds(DockerClient apiClient) throws DockerException, InterruptedException {
        List<Container> containers = apiClient.listContainers(DockerClient.ListContainersParam.allContainers());
        return containers.stream().map((container -> container.id())).collect(Collectors.toList());
    }

    public NmServiceDeploymentState checkService(String containerId, DockerHost host)
            throws ContainerNotFoundException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            return executeInspectContainerAndReturnContainerState(containerId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not check service state -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private NmServiceDeploymentState executeInspectContainerAndReturnContainerState(String containerId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        ContainerInfo containerInfo = apiClient.inspectContainer(containerId);
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
