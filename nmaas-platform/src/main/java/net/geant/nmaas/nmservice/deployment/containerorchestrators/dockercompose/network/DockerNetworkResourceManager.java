package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
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

    public int obtainPortForClientNetwork(String domain, Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForDomain(domain);
        try {
            return dockerHostStateKeeper.assignPortForContainer(network.getHost().getName(), deploymentId);
        } catch (DockerHostNotFoundException e) {
            throw new ContainerOrchestratorInternalErrorException("Problems with port assignment for container -> " + e.getMessage());
        }
    }

    public String obtainDeploymentNameFromClientNetwork(String domain) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForDomain(domain);
        return network.getDeploymentName();
    }

    public String obtainSubnetFromClientNetwork(String domain) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForDomain(domain);
        return network.getSubnet();
    }

    public String obtainGatewayFromClientNetwork(String domain) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForDomain(domain);
        return network.getGateway();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String assignNewIpAddressForContainer(String domain) throws ContainerOrchestratorInternalErrorException {
        final DockerHostNetwork network = networkForDomain(domain);
        String address = findNewAddress(network);
        updateNetworkWithNewAssignedAddress(domain, address);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeAddressAssignment(String domain, String previouslyAssignedAddress)
            throws ContainerOrchestratorInternalErrorException {
        try {
            List<String> assignedAddresses = new ArrayList<>(repositoryManager.loadNetwork(domain).getAssignedAddresses());
            assignedAddresses.remove(previouslyAssignedAddress);
            repositoryManager.updateAssignedAddresses(domain, assignedAddresses);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    private DockerHostNetwork networkForDomain(String domain) throws ContainerOrchestratorInternalErrorException {
        try {
            return repositoryManager.loadNetwork(domain);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

    private void updateNetworkWithNewAssignedAddress(String domain, String address) throws ContainerOrchestratorInternalErrorException {
        try {
            final DockerHostNetwork network = repositoryManager.loadNetwork(domain);
            List<String> assignedAddresses = new ArrayList<>(network.getAssignedAddresses());
            assignedAddresses.add(address);
            repositoryManager.updateAssignedAddresses(domain, assignedAddresses);
        } catch (InvalidDomainException ide) {
            throw new ContainerOrchestratorInternalErrorException(ide.getMessage());
        }
    }

}
