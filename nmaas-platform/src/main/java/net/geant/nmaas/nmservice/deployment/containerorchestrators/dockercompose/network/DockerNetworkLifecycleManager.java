package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotCreateContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkDetailsVerificationException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void declareNewNetworkForClientOnHost(String domain, DockerHost dockerHost) throws ContainerOrchestratorInternalErrorException {
        DockerHostNetwork dockerHostNetwork = new DockerHostNetwork(domain, dockerHost);
        repositoryManager.storeNetwork(dockerHostNetwork);
        try {
            dockerHostNetwork = repositoryManager.loadNetwork(domain);
            int assignedVlan = dockerHostStateKeeper.assignVlanForNetwork(dockerHost.getName(), domain);
            dockerHostNetwork.setVlanNumber(assignedVlan);
            DockerNetworkIpam assignedAddresses = dockerHostStateKeeper.assignAddressPoolForNetwork(dockerHost.getName(), domain);
            dockerHostNetwork.setSubnet(assignedAddresses.getSubnetWithMask());
            dockerHostNetwork.setGateway(assignedAddresses.getGateway());
            repositoryManager.updateNetwork(dockerHostNetwork);
        } catch (DockerHostNotFoundException dhnfe) {
            throw new ContainerOrchestratorInternalErrorException("Internal error -> " + dhnfe.getMessage());
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    public boolean networkForDomainAlreadyConfigured(String domain) {
        return repositoryManager.checkNetwork(domain);
    }

    public DockerHostNetwork networkForDomain(String domain) throws ContainerOrchestratorInternalErrorException {
        try {
            return repositoryManager.loadNetwork(domain);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    public String deployNetworkForDomain(String domain) throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerHostNetwork network = networkForDomain(domain);
            if (networkAlreadyDeployed(network))
                return network.getDeploymentName();
            final NetworkConfig networkConfig = DockerNetworkConfigBuilder.build(network);
            final String networkId = executeCreateNetwork(networkConfig, network.getHost().apiUrl());
            repositoryManager.updateNetworkIdAndNetworkName(domain, networkId, networkConfig.name());
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
            Thread.currentThread().interrupt();
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for domain " + domain, ide);
        }
    }

    private boolean networkAlreadyDeployed(DockerHostNetwork network) {
        return network.getDeploymentId() != null;
    }

    private String executeCreateNetwork(NetworkConfig networkConfig, String apiUrl)
            throws DockerException, InterruptedException {
        return dockerApiClient.createNetwork(apiUrl, networkConfig);
    }

    public void verifyNetwork(String domain) throws DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        DockerHostNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(domain);
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
            Thread.currentThread().interrupt();
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    private void executeCheckNetwork(String networkId, String apiUrl)
            throws DockerNetworkCheckFailedException, DockerException, InterruptedException {
        if (dockerApiClient.listNetworks(apiUrl).stream().noneMatch(id -> id.equals(networkId)))
            throw new DockerNetworkCheckFailedException("Network with given id " + networkId + " not exists on Docker Host");
    }

    public void removeNetwork(String domain)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        DockerHostNetwork network = null;
        try {
            network = repositoryManager.loadNetwork(domain);
            repositoryManager.removeNetwork(domain);
            if (checkIfNetworkExists(network.getDeploymentId(), network.getHost().apiUrl()))
                executeRemove(network.getDeploymentId(), network.getHost().apiUrl());
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotRemoveContainerNetworkException(
                    "Could not removeIfNoContainersAttached container network " + network.getDeploymentId() + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ContainerOrchestratorInternalErrorException(
                    "Internal error -> " + interruptedException.getMessage(), interruptedException);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    private boolean checkIfNetworkExists(String networkId, String apiUrl) throws DockerException, InterruptedException {
        return dockerApiClient.listNetworks(apiUrl).stream().anyMatch(n -> n.equals(networkId));
    }

    private void executeRemove(String networkId, String apiUrl) throws DockerException, InterruptedException {
        dockerApiClient.removeNetwork(apiUrl, networkId);
    }

}