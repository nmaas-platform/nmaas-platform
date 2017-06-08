package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerHostStateKeeper {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private Map<String, DockerHostState> states = new HashMap<>();

    public int assignPortForContainer(String dockerHostName, DockerContainer container) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDockerHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignPort(container);
    }

    public void removePortAssignment(String dockerHostName, List<Integer> ports) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removePortAssignment(ports);
    }

    public int getAssignedPort(String dockerHostName, DockerContainer container) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedPorts(container).get(0);
    }

    public int assignVlanForNetwork(String dockerHostName, DockerNetwork network) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDockerHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignVlan(network);
    }

    public void removeVlanAssignment(String dockerHostName, int vlanNumber) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removeVlanAssignment(vlanNumber);
    }

    public int getAssignedVlan(String dockerHostName, DockerNetwork network) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedVlan(network);
    }

    public DockerNetworkIpamSpec assignAddressPoolForNetwork(String dockerHostName, DockerNetwork network) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDockerHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignAddresses(network);
    }

    public void removeAddressPoolAssignment(String dockerHostName, DockerNetworkIpamSpec addressPool) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removeAddressPoolAssignment(addressPool);
    }

    public DockerNetworkIpamSpec getAssignedAddressPool(String dockerHostName, DockerNetwork network) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedAddressPool(network);
    }

    private void addStateForDockerHostIfAbsent(String dockerHostName) throws DockerHostNotFoundException, DockerHostInvalidException {
        if (!states.containsKey(dockerHostName)) {
            String dockerHostBaseDataNetworkAddress = dockerHostRepositoryManager.loadByName(dockerHostName).getBaseDataNetworkAddress().getHostAddress();
            final DockerHostState state = new DockerHostState(dockerHostBaseDataNetworkAddress);
            states.put(dockerHostName, state);
        }
    }
}
