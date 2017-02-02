package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDestroyNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.OrchestratorInternalErrorException;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerApiClientFactory.client;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerContainerClient {

    public String deploy(ContainerConfig containerConfig, String name, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            return executeDeploy(containerConfig, name, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException(
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
            throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            executeStopAndRemove(containerId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException(
                    "Could not remove container " + containerId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException(
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
            throws OrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            executePullImage(imageName, apiClient);
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException(
                    "Could not pull image " + imageName + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException(
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
            throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            return executeListContainersAndReturnIds(apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private List<String> executeListContainersAndReturnIds(DockerClient apiClient) throws DockerException, InterruptedException {
        List<Container> containers = apiClient.listContainers(DockerClient.ListContainersParam.allContainers());
        return containers.stream().map((container -> container.id())).collect(Collectors.toList());
    }

    public NmServiceState checkService(String containerId, DockerHost host)
            throws net.geant.nmaas.servicedeployment.exceptions.ContainerNotFoundException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            return executeInspectContainerAndReturnContainerState(containerId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException(
                    "Could not check service state -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private NmServiceState executeInspectContainerAndReturnContainerState(String containerId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        ContainerInfo containerInfo = apiClient.inspectContainer(containerId);
        return serviceStateFromContainerInfo(containerInfo);
    }

    private NmServiceState serviceStateFromContainerInfo(ContainerInfo containerInfo) {
        switch(containerInfo.state().status()) {
            case "exited":
                return NmServiceState.REMOVED;
            case "running":
                return NmServiceState.DEPLOYED;
                default:
                    return NmServiceState.ERROR;
        }
    }
}
