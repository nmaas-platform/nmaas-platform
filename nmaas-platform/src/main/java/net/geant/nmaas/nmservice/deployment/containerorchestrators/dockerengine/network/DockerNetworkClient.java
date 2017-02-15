package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClientFactory.client;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerNetworkClient {

    public String create(NetworkConfig networkConfig, DockerHost host)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            return executeCreateNetwork(networkConfig, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotCreateContainerNetworkException(
                    "Could not create container network " + networkConfig.name() + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private String executeCreateNetwork(NetworkConfig networkConfig, DockerClient apiClient)
            throws DockerException, InterruptedException {
        return apiClient.createNetwork(networkConfig).id();
    }

    public void checkNetwork(String networkId, String containerId, DockerHost host)
            throws ContainerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            executeCheckNetwork(networkId, containerId, apiClient);
        } catch (NetworkNotFoundException networkNotFoundException) {
            throw new ContainerNetworkCheckFailedException(
                    "Network not verified (not found by provided id: " + networkId + " ) -> " + networkNotFoundException.getMessage());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not execute requested action on Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private void executeCheckNetwork(String networkId, String containerId, DockerClient apiClient)
            throws ContainerNetworkCheckFailedException, DockerException, InterruptedException {
            Network network = apiClient.inspectNetwork(networkId);
        if (network.containers().isEmpty() || network.containers().get(containerId) == null)
            throw new ContainerNetworkCheckFailedException("Container network check failed (id: " + networkId + ")");
    }

    public void connectContainerToNetwork(String containerId, String networkId, DockerHost host)
            throws CouldNotConnectContainerToNetworkException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            executeConnectContainerToNetwork(containerId, networkId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotConnectContainerToNetworkException(
                    "Could not connect container " + containerId + " to network " + networkId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private void executeConnectContainerToNetwork(String containerId, String networkId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        apiClient.connectToNetwork(containerId, networkId);
    }

    public void remove(String networkId, DockerHost host)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = client(host.apiUrl());
        try {
            executeRemove(networkId, apiClient);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotRemoveContainerNetworkException(
                    "Could not remove container network " + networkId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    private void executeRemove(String networkId, DockerClient apiClient)
            throws DockerException, InterruptedException {
        apiClient.removeNetwork(networkId);
    }

}
