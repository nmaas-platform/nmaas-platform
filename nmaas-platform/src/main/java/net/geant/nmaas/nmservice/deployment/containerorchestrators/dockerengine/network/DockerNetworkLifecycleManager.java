package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.EndpointConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkConnection;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpam;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerNetworkLifecycleManager {

    private DockerHostNetworkRepositoryManager repositoryManager;

    private DockerHostStateKeeper dockerHostStateKeeper;

    private DockerApiClient dockerApiClient;

    @Autowired
    public DockerNetworkLifecycleManager(DockerHostNetworkRepositoryManager repositoryManager, DockerHostStateKeeper dockerHostStateKeeper, DockerApiClient dockerApiClient) {
        this.repositoryManager = repositoryManager;
        this.dockerHostStateKeeper = dockerHostStateKeeper;
        this.dockerApiClient = dockerApiClient;
    }

    public void declareNewNetworkForClientOnHost(Identifier clientId, DockerHost dockerHost) throws ContainerOrchestratorInternalErrorException {
        DockerHostNetwork dockerHostNetwork = new DockerHostNetwork(clientId, dockerHost);
        repositoryManager.storeNetwork(dockerHostNetwork);
        try {
            dockerHostNetwork = repositoryManager.loadNetwork(clientId);
            int assignedVlan = dockerHostStateKeeper.assignVlanForNetwork(dockerHost.getName(), clientId);
            dockerHostNetwork.setVlanNumber(assignedVlan);
            DockerNetworkIpam assignedAddresses = dockerHostStateKeeper.assignAddressPoolForNetwork(dockerHost.getName(), clientId);
            dockerHostNetwork.setSubnet(assignedAddresses.getSubnetWithMask());
            dockerHostNetwork.setGateway(assignedAddresses.getGateway());
            repositoryManager.updateNetwork(dockerHostNetwork);
        } catch (DockerHostNotFoundException e) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + e.getMessage(), e);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    public boolean networkForClientAlreadyConfigured(Identifier clientId) {
        return repositoryManager.checkNetwork(clientId);
    }

    public DockerHostNetwork networkForClient(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        try {
            return repositoryManager.loadNetwork(clientId);
        } catch (InvalidClientIdException e) {
            throw new ContainerOrchestratorInternalErrorException("No network found in repository for client " + clientId);
        }
    }

    public String deployNetworkForClient(Identifier clientId) throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerHostNetwork network = networkForClient(clientId);
            if (networkAlreadyDeployed(network))
                return network.getDeploymentName();
            final NetworkConfig networkConfig = DockerNetworkConfigBuilder.build(network);
            final String networkId = executeCreateNetwork(networkConfig, network.getHost().apiUrl());
            repositoryManager.updateNetworkIdAndNetworkName(clientId, networkId, networkConfig.name());
            return networkConfig.name();
        } catch (DockerNetworkDetailsVerificationException verificationException) {
            throw new CouldNotCreateContainerNetworkException(
                    "Network specification verification failed -> " + verificationException.getMessage(), verificationException);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotCreateContainerNetworkException(
                    "Could not create container network -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    private boolean networkAlreadyDeployed(DockerHostNetwork network) {
        return network.getDeploymentId() != null;
    }

    private String executeCreateNetwork(NetworkConfig networkConfig, String apiUrl)
            throws DockerException, InterruptedException {
        return dockerApiClient.createNetwork(apiUrl, networkConfig);
    }

    public void verifyNetwork(Identifier clientId) throws DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        DockerHostNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            executeCheckNetwork(network.getDeploymentId(), network.getHost().apiUrl());
        } catch (NetworkNotFoundException networkNotFoundException) {
            throw new DockerNetworkCheckFailedException(
                    "Docker network with id " + network.getDeploymentId() + " not found on the Docker Host -> " + networkNotFoundException.getMessage());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not execute requested action on Docker Engine -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    private void executeCheckNetwork(String networkId, String apiUrl)
            throws DockerNetworkCheckFailedException, DockerException, InterruptedException {
        if (dockerApiClient.listNetworks(apiUrl).stream().noneMatch(id -> id.equals(networkId)))
            throw new DockerNetworkCheckFailedException("Network with given id " + networkId + " not exists on Docker Host");
    }

    public void connectContainerToNetwork(Identifier clientId, DockerContainer container)
            throws CouldNotConnectContainerToNetworkException, ContainerOrchestratorInternalErrorException {
        DockerHostNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            executeConnectContainerToNetwork(network.getDeploymentId(), container, network.getHost().apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotConnectContainerToNetworkException(
                    "Could not connect container " + container.getDeploymentId() + " to network " + network.getDeploymentId() + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    private void executeConnectContainerToNetwork(String networkId, DockerContainer container, String apiUrl)
            throws DockerException, InterruptedException {
        String containerIpAddress = container.getNetworkDetails().getIpam().getIpAddressOfContainer();
        final EndpointConfig endpointConfig =
                EndpointConfig.builder()
                        .ipamConfig(
                                EndpointConfig.EndpointIpamConfig.builder()
                                        .ipv4Address(containerIpAddress)
                                        .build())
                        .build();
        final NetworkConnection networkConnection =
                NetworkConnection.builder()
                        .containerId(container.getDeploymentId())
                        .endpointConfig(endpointConfig)
                        .build();
        dockerApiClient.connectToNetwork(apiUrl, networkId, networkConnection);
    }

    /**
     * Removes the given container from the list of containers attached to given client's network.
     * No action is triggered on the Docker Host. It is assumed that the container has been already removed
     * from the Docker Host and thus not attached to the network anymore.
     *
     * @param clientId Identifier of the client
     * @param container Container being disconnected
     * @throws ContainerOrchestratorInternalErrorException if network for given client not found in repository
     */
    public void disconnectContainerFromNetwork(Identifier clientId, DockerContainer container)
            throws ContainerOrchestratorInternalErrorException {
    }

    public void removeNetwork(Identifier clientId)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        DockerHostNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            repositoryManager.removeNetwork(clientId);
            if (checkIfNetworkExists(network.getDeploymentId(), network.getHost().apiUrl()))
                executeRemove(network.getDeploymentId(), network.getHost().apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotRemoveContainerNetworkException(
                    "Could not removeIfNoContainersAttached container network " + network.getDeploymentId() + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    private boolean checkIfNetworkExists(String networkId, String apiUrl) throws DockerException, InterruptedException {
        return dockerApiClient.listNetworks(apiUrl).stream().anyMatch(n -> n.equals(networkId));
    }

    private void executeRemove(String networkId, String apiUrl) throws DockerException, InterruptedException {
        dockerApiClient.removeNetwork(apiUrl, networkId);
    }

}