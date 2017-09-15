package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpam;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerNetworkResourceManager {

    private DockerHostNetworkRepositoryManager repositoryManager;

    private DockerHostStateKeeper dockerHostStateKeeper;

    @Autowired
    public DockerNetworkResourceManager(DockerHostNetworkRepositoryManager repositoryManager, DockerHostStateKeeper dockerHostStateKeeper) {
        this.repositoryManager = repositoryManager;
        this.dockerHostStateKeeper = dockerHostStateKeeper;
    }

    public int obtainPortForClientNetwork(Identifier clientId, Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForClient(clientId);
        try {
            return dockerHostStateKeeper.assignPortForContainer(network.getHost().getName(), deploymentId);
        } catch (DockerHostNotFoundException e) {
            throw new ContainerOrchestratorInternalErrorException("Problems with port assignment for container -> " + e.getMessage());
        }
    }

    public String obtainDeploymentNameFromClientNetwork(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForClient(clientId);
        return network.getDeploymentName();
    }

    public String obtainSubnetFromClientNetwork(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForClient(clientId);
        return network.getSubnet();
    }

    public String obtainGatewayFromClientNetwork(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForClient(clientId);
        return network.getGateway();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String assignNewIpAddressForContainer(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForClient(clientId);
        String address = findNewAddress(network);
        updateNetworkWithNewAssignedAddress(clientId, address);
        return address;
    }

    private String findNewAddress(DockerHostNetwork network) {
        String address = DockerNetworkIpam.obtainFirstIpAddressFromNetwork(network.getSubnet());
        while(addressAlreadyAssigned(network.getAssignedAddresses(), address))
            address = DockerNetworkIpam.obtainNextIpAddressFromNetwork(address);
        return address;
    }

    private boolean addressAlreadyAssigned(List<String> addressesAlreadyAssigned, String checkedAddress) {
        return addressesAlreadyAssigned.contains(checkedAddress);
    }

    public void removeAddressAssignment(Identifier clientId, String previouslyAssignedAddress)
            throws ContainerOrchestratorInternalErrorException {
        try {
            List<String> assignedAddresses = repositoryManager.loadNetwork(clientId).getAssignedAddresses();
            assignedAddresses.remove(previouslyAssignedAddress);
            repositoryManager.updateAssignedAddresses(clientId, assignedAddresses);
        } catch (InvalidClientIdException invalidClientIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "No network found in repository for client " + clientId, invalidClientIdException);
        }
    }

    private DockerHostNetwork networkForClient(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        try {
            return repositoryManager.loadNetwork(clientId);
        } catch (InvalidClientIdException e) {
            throw new ContainerOrchestratorInternalErrorException("No network found in repository for client " + clientId);
        }
    }

    private void updateNetworkWithNewAssignedAddress(Identifier clientId, String address) throws ContainerOrchestratorInternalErrorException {
        try {
            final DockerHostNetwork network = repositoryManager.loadNetwork(clientId);
            List<String> assignedAddresses = new ArrayList<>(network.getAssignedAddresses());
            assignedAddresses.add(address);
            repositoryManager.updateAssignedAddresses(clientId, assignedAddresses);
        } catch (InvalidClientIdException e) {
            throw new ContainerOrchestratorInternalErrorException("No network found in repository for client " + clientId);
        }
    }

}
