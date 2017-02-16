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
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerContainerClient {

    public String create(ContainerConfig containerConfig, String name, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            return executeCreate(containerConfig, name, apiClient);
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

    private String executeCreate(ContainerConfig containerConfig, String name, DockerClient apiClient)
            throws DockerException, InterruptedException {
        final ContainerCreation container = apiClient.createContainer(containerConfig, name);
        return container.id();
    }

    public void start(String containerId, DockerHost host)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            executeStart(containerId, apiClient);
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

    private void executeStart(String containerId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        apiClient.startContainer(containerId);
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

    public void checkService(String containerId, DockerHost host)
            throws ContainerCheckFailedException, ContainerNotFoundException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(host.apiUrl());
        try {
            if (!NmServiceDeploymentState.DEPLOYED.equals(executeInspectContainerAndReturnContainerState(containerId, apiClient)))
                throw new ContainerCheckFailedException("Container with id " + containerId + " is stopped");
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