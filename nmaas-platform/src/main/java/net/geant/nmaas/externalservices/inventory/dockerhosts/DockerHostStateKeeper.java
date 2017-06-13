package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.repositories.DockerHostStateRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the assignment and persistence of Docker Host network related resources, namely ports, VLANs and IP addresses.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerHostStateKeeper {

    private static final int MIN_ASSIGNABLE_PORT_NUMBER = 1000;
    private static final int MIN_ASSIGNABLE_VLAN_NUMBER = 500;
    private static final int ADDRESS_POOL_MIN_ASSIGNABLE_ADDRESS = 1;
    private static final int ADDRESS_POOL_DEFAULT_GATEWAY = 254;
    private static final int ADDRESS_POOL_DEFAULT_MASK_LENGTH = 24;

    @Autowired
    private DockerHostStateRepository stateRepository;

    public int assignPortForContainer(String dockerHostName, Identifier deploymentId) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDockerHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignPort(container);
    }

    public void removePortAssignment(String dockerHostName, Identifier deploymentId) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removePortAssignment(ports);
    }

    public int getAssignedPort(String dockerHostName, Identifier deploymentId) throws DockerHostNotFoundException, MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedPorts(container).get(0);
    }

    public int assignVlanForNetwork(String dockerHostName, Identifier clientId) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDockerHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignVlan(network);
    }

    public void removeVlanAssignment(String dockerHostName, Identifier clientId) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removeVlanAssignment(vlanNumber);
    }

    public int getAssignedVlan(String dockerHostName, Identifier clientId) throws DockerHostNotFoundException, MappingNotFoundException {
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

    /**
     * Checks currently assigned ports on the host, assigns a new port and returns its number.
     * Currently it is assumed that a given NM service/tool/container will expose only a single public interface
     * that the client should use to access the GUI.
     *
     * @param container Docker container
     * @return Assigned port
     */
    int assignPort(DockerContainer container) {
        return assignPorts(1, container).get(0);
    }

    /**
     * Checks currently assigned ports on the host, assigns requested number of new ports and returns a list of them.
     *
     * @param number Number of ports to be assigned for given service/container
     * @param container Docker container
     * @return List of assigned ports
     */
    private List<Integer> assignPorts(int number, DockerContainer container) {
        List<Integer> newAssignedPorts = new ArrayList<>();
        int count = 0;
        int portNumber = MIN_ASSIGNABLE_PORT_NUMBER;
        while(count < number) {
            while(assignedPorts.keySet().contains(portNumber))
                portNumber++;
            newAssignedPorts.add(portNumber);
            count++;
            portNumber++;
        }
        newAssignedPorts.forEach((port) -> assignedPorts.put(port, container));
        return newAssignedPorts;
    }

    void removePortAssignment(List<Integer> ports) {
        ports.forEach((port) -> assignedPorts.remove(port));
    }

    List<Integer> getAssignedPorts(DockerContainer container) throws MappingNotFoundException {
        List<Integer> foundPorts = assignedPorts.entrySet().stream()
                .filter(entry -> entry.getValue().equals(container))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        if (foundPorts.isEmpty())
            throw new MappingNotFoundException("No ports found for container " + container.getId());
        return foundPorts;
    }

    /**
     * Checks currently assigned VLANs on the host, assigns new VLAN and returns its number.
     *
     * @param network Docker network
     * @return Number of assigned VLAN
     */
    int assignVlan(DockerNetwork network) {
        int vlanNumber = MIN_ASSIGNABLE_VLAN_NUMBER;
        while (assignedVlans.keySet().contains(vlanNumber))
            vlanNumber++;
        assignedVlans.put(vlanNumber, network);
        return vlanNumber;
    }

    void removeVlanAssignment(int vlanNumber) {
        assignedVlans.remove(vlanNumber);
    }

    int getAssignedVlan(DockerNetwork network) throws MappingNotFoundException {
        Map.Entry<Integer, DockerNetwork> foundMapping = assignedVlans.entrySet().stream()
                .filter(entry -> entry.getValue().equals(network))
                .findFirst()
                .orElseThrow(() -> new MappingNotFoundException("No VLAN number found for network " + network.getId()));
        return foundMapping.getKey();
    }

    /**
     * Checks currently assigned address pools on the host, assigns new pool and returns it.
     *
     * @param network Docker network
     * @return Assigned address pool
     */
    DockerNetworkIpamSpec assignAddresses(DockerNetwork network) {
        int address = ADDRESS_POOL_MIN_ASSIGNABLE_ADDRESS;
        DockerNetworkIpamSpec candidateAddressPool = null;
        do {
            candidateAddressPool = DockerNetworkIpamSpec.fromParameters(
                    dockerHostAddressPoolBase,
                    address,
                    ADDRESS_POOL_DEFAULT_GATEWAY,
                    ADDRESS_POOL_DEFAULT_MASK_LENGTH);
            address++;
        } while (assignedAddressPools.containsKey(candidateAddressPool));
        assignedAddressPools.put(candidateAddressPool, network);
        return candidateAddressPool;
    }

    void removeAddressPoolAssignment(DockerNetworkIpamSpec addressPool) {
        assignedAddressPools.remove(addressPool);
    }

    DockerNetworkIpamSpec getAssignedAddressPool(DockerNetwork network) throws MappingNotFoundException {
        Map.Entry<DockerNetworkIpamSpec, DockerNetwork> foundMapping = assignedAddressPools.entrySet().stream()
                .filter(entry -> entry.getValue().equals(network))
                .findFirst()
                .orElseThrow(() -> new MappingNotFoundException("No address pool found for network " + network.getId()));
        return foundMapping.getKey();
    }

    public class MappingNotFoundException extends Exception {
        public MappingNotFoundException(String message) {
            super(message);
        }
    }
}
