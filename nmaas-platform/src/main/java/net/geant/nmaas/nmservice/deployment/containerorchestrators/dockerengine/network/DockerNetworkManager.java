package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.EndpointConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkConnection;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerNetworkManager {

    private DockerNetworkRepositoryManager repositoryManager;

    private DockerHostStateKeeper dockerHostStateKeeper;

    private DockerApiClient dockerApiClient;

    @Autowired
    public DockerNetworkManager(DockerNetworkRepositoryManager repositoryManager, DockerHostStateKeeper dockerHostStateKeeper, DockerApiClient dockerApiClient) {
        this.repositoryManager = repositoryManager;
        this.dockerHostStateKeeper = dockerHostStateKeeper;
        this.dockerApiClient = dockerApiClient;
    }

    public void declareNewNetworkForClientOnHost(Identifier clientId, DockerHost dockerHost) throws ContainerOrchestratorInternalErrorException {
        DockerNetwork dockerNetwork = new DockerNetwork(clientId, dockerHost);
        repositoryManager.storeNetwork(dockerNetwork);
        try {
            dockerNetwork = repositoryManager.loadNetwork(clientId);
            int assignedVlan = dockerHostStateKeeper.assignVlanForNetwork(dockerHost.getName(), dockerNetwork);
            dockerNetwork.setVlanNumber(assignedVlan);
            DockerNetworkIpamSpec assignedAddresses = dockerHostStateKeeper.assignAddressPoolForNetwork(dockerHost.getName(), dockerNetwork);
            dockerNetwork.setSubnet(assignedAddresses.getSubnetWithMask());
            dockerNetwork.setGateway(assignedAddresses.getGateway());
            repositoryManager.updateNetwork(dockerNetwork);
        } catch (DockerHostNotFoundException
                | DockerHostInvalidException e) {
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

    public DockerNetwork networkForClient(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        try {
            return repositoryManager.loadNetwork(clientId);
        } catch (InvalidClientIdException e) {
            throw new ContainerOrchestratorInternalErrorException("No network found in repository for client " + clientId);
        }
    }

    public String deployNetworkForClient(Identifier clientId) throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerNetwork network = repositoryManager.loadNetwork(clientId);
            if (networkAlreadyDeployed(network))
                return network.getDeploymentName();
            final NetworkConfig networkConfig = DockerNetworkConfigBuilder.build(network);
            final String networkId = executeCreateNetwork(networkConfig, network.getDockerHost().apiUrl());
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

    private boolean networkAlreadyDeployed(DockerNetwork network) {
        return network.getDeploymentId() != null;
    }

    private String executeCreateNetwork(NetworkConfig networkConfig, String apiUrl)
            throws DockerException, InterruptedException {
        return dockerApiClient.createNetwork(apiUrl, networkConfig);
    }

    public void verifyNetwork(Identifier clientId) throws DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        DockerNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            executeCheckNetwork(
                    network.getDeploymentId(),
                    network.getDockerContainers().stream().map(c -> c.getDeploymentId()).collect(Collectors.toList()),
                    network.getDockerHost().apiUrl());
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

    private void executeCheckNetwork(String networkId, List<String> localContainers, String apiUrl)
            throws DockerNetworkCheckFailedException, DockerException, InterruptedException {
        int noOfRemoteContainers = dockerApiClient.countContainersInNetwork(apiUrl, networkId);
        verifyIfAllContainersPresent(networkId, noOfRemoteContainers, localContainers);
    }

    void verifyIfAllContainersPresent(String networkId, int noOfRemoteContainers, List<String> localContainers) throws DockerNetworkCheckFailedException {
        if (localContainers.size() > 0 && noOfRemoteContainers == 0)
            throw new DockerNetworkCheckFailedException("Docker network " + networkId + " verification failed. None containers attached.");
        if (localContainers.size() != noOfRemoteContainers)
            throw new DockerNetworkCheckFailedException("Docker network " + networkId + " verification failed. Some containers not attached.");
    }

    public DockerContainerNetDetails obtainNetworkDetailsForContainer(Identifier clientId, Identifier deploymentId)
            throws ContainerOrchestratorInternalErrorException {
        try {
            final DockerNetwork network = networkForClient(clientId);
            final int assignedPublicPort = dockerHostStateKeeper.assignPortForContainer(network.getDockerHost().getName(), deploymentId);
            String containerIpAddress = obtainIpAddressForNewContainer(network);
            final DockerNetworkIpamSpec addresses = new DockerNetworkIpamSpec(containerIpAddress, network.getSubnet(), network.getGateway());
            return new DockerContainerNetDetails(assignedPublicPort, addresses);
        } catch (DockerHostNotFoundException
                | DockerHostInvalidException e) {
            throw new ContainerOrchestratorInternalErrorException("Problems with port assignment for container -> " + e.getMessage());
        }
    }

    String obtainIpAddressForNewContainer(DockerNetwork network) {
        String containerIpAddress = DockerNetworkIpamSpec.obtainFirstIpAddressFromNetwork(network.getSubnet());
        while(addressAlreadyAssigned(containerIpAddress, network.getDockerContainers()))
            containerIpAddress = DockerNetworkIpamSpec.obtainNextIpAddressFromNetwork(containerIpAddress);
        return containerIpAddress;
    }

    boolean addressAlreadyAssigned(String containerIpAddress, List<DockerContainer> dockerContainers) {
        return dockerContainers.stream()
                .map(c -> c.getNetworkDetails().getIpAddresses().getIpAddressOfContainer())
                .anyMatch(s -> s.equals(containerIpAddress));
    }

    public void addContainerToNetwork(Identifier clientId, DockerContainer container)
            throws ContainerOrchestratorInternalErrorException {
        try {
            repositoryManager.updateAddContainer(clientId, container);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    public void connectContainerToNetwork(Identifier clientId, DockerContainer container)
            throws CouldNotConnectContainerToNetworkException, ContainerOrchestratorInternalErrorException {
        DockerNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            repositoryManager.updateAddContainer(clientId, container);
            executeConnectContainerToNetwork(network.getDeploymentId(), container, network.getDockerHost().apiUrl());
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
        String containerIpAddress = container.getNetworkDetails().getIpAddresses().getIpAddressOfContainer();
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

    public void removeContainerFromNetwork(Identifier clientId, Long containerId)
            throws ContainerOrchestratorInternalErrorException {
        try {
            repositoryManager.updateRemoveContainer(clientId, containerId);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    /**
     * Removes the given container from the list of containers attached to given client's network.
     * No action is triggered on the Docker Host. It is assumed that the container has been already removed
     * from the Docker Host and thus not attached to the network anymore.
     *
     * @param clientId Identifier of the client
     * @param containerId Identifier of the container being disconnected
     * @throws ContainerOrchestratorInternalErrorException if network for given client not found in repository
     */
    public void disconnectContainerFromNetwork(Identifier clientId, String containerId)
            throws ContainerOrchestratorInternalErrorException {
        try {
            repositoryManager.updateRemoveContainer(clientId, containerId);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    public void removeIfNoContainersAttached(Identifier clientId)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        DockerNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(clientId);
            if (network.getDockerContainers().isEmpty()) {
                repositoryManager.removeNetwork(clientId);
                if (checkIfNetworkExists(network.getDeploymentId(), network.getDockerHost().apiUrl()))
                    executeRemove(network.getDeploymentId(), network.getDockerHost().apiUrl());
            }
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