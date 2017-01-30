package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerContainerClient {

    public String deploy(ContainerConfig containerConfig, String name, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.apiUrl()).build();
            final ContainerCreation container = apiClient.createContainer(containerConfig, name);
            final String containerId = container.id();
            apiClient.startContainer(containerId);
            return containerId;
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void remove(String containerId, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.apiUrl()).build();
            apiClient.stopContainer(containerId, 3);
            apiClient.removeContainer(containerId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not remove container " + containerId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void pullImage(String imageName, DockerHost host)
            throws CouldNotDestroyNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.apiUrl()).build();
            apiClient.pull(imageName);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not pull image " + imageName + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public List<String> containers(DockerHost host) throws OrchestratorInternalErrorException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.apiUrl()).build();
            List<Container> containers = apiClient.listContainers(DockerClient.ListContainersParam.allContainers());
            return containers.stream().map((container -> container.id())).collect(Collectors.toList());
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException("Could not connect to Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public NmServiceState checkService(String containerId, DockerHost host) throws net.geant.nmaas.servicedeployment.exceptions.ContainerNotFoundException, OrchestratorInternalErrorException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.apiUrl()).build();
            ContainerInfo containerInfo = apiClient.inspectContainer(containerId);
            return serviceStateFromContainerInfo(containerInfo);
        } catch (ContainerNotFoundException containerNotFoundException) {
            throw new net.geant.nmaas.servicedeployment.exceptions.ContainerNotFoundException("Container not found on Docker Host -> " + containerNotFoundException.getMessage(), containerNotFoundException);
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException("Could not connect to Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
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
