package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHostState {

    public static final int MIN_ASSIGNABLE_PORT_NUMBER = 1000;

    public static final int MIN_ASSIGNABLE_VLAN_NUMBER = 500;

    public static final int ADDRESS_POOL_MIN_ASSIGNABLE_NETWORK = 1;

    public static final int ADDRESS_POOL_DEFAULT_GATEWAY = 254;

    public static final int ADDRESS_POOL_DEFAULT_MASK_LENGTH = 24;

    private final String dockerHostName;

    private final String dockerHostAddressPoolBase;

    /**
     * Map of ports on the public interface currently assigned for containers/services deployed on the host.
     */
    private final Map<Integer, String> assignedPorts = new HashMap<>();

    /**
     * Map of numbers of VLANs currently configured on the data interface for containers/services deployed on the host.
     */
    private final Map<Integer, String> assignedVlans = new HashMap<>();

    /**
     * Map of objects representing pool of addresses assigned for containers/services deployed on the host.
     */
    private final Map<ContainerNetworkIpamSpec, String> assignedAddressPools = new HashMap<>();

    public DockerHostState(String dockerHostName, String dockerHostAddressPoolBase) {
        this.dockerHostName = dockerHostName;
        this.dockerHostAddressPoolBase = dockerHostAddressPoolBase;
    }

    /**
     * Checks currently assigned ports on the host, assigns requested number of new ports and returns a list of them.
     *
     * @param number Number of ports to be assigned for given service
     * @param serviceName Name of the service
     * @return List of assigned ports
     */
    public List<Integer> assignPorts(int number, String serviceName) {
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
        newAssignedPorts.forEach((port) -> assignedPorts.put(port, serviceName));
        return newAssignedPorts;
    }

    public void removePortAssignment(List<Integer> ports) {
        ports.forEach((port) -> assignedPorts.remove(port));
    }

    public List<Integer> getAssignedPorts(String serviceName) throws MappingNotFoundException {
        List<Integer> foundPorts = assignedPorts.entrySet().stream()
                .filter(entry -> entry.getValue().equals(serviceName))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        if (foundPorts.isEmpty())
            throw new MappingNotFoundException("No ports found for service " + serviceName);
        return foundPorts;
    }

    /**
     * Checks currently assigned VLANs on the host, assigns new VLAN and returns its number.
     *
     * @return VLAN number
     */
    public int assignVlan(String serviceName) {
        int vlanNumber = MIN_ASSIGNABLE_VLAN_NUMBER;
        while (assignedVlans.keySet().contains(vlanNumber))
            vlanNumber++;
        assignedVlans.put(vlanNumber, serviceName);
        return vlanNumber;
    }

    public void removeVlanAssignment(int vlanNumber) {
        assignedVlans.remove(vlanNumber);
    }

    public int getAssignedVlan(String serviceName) throws MappingNotFoundException {
        Map.Entry<Integer, String> foundMapping = assignedVlans.entrySet().stream()
                .filter(entry -> entry.getValue().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new MappingNotFoundException("No VLAN number found for service " + serviceName));
        return foundMapping.getKey();
    }

    /**
     * Checks currently assigned address pools on the host, assigns new pool and returns it.
     *
     * @param serviceName Name of the service
     * @return Assigned address pool
     */
    public ContainerNetworkIpamSpec assignAddresses(String serviceName) {
        int network = ADDRESS_POOL_MIN_ASSIGNABLE_NETWORK;
        ContainerNetworkIpamSpec candidateAddressPool = null;
        do {
            candidateAddressPool = ContainerNetworkIpamSpec.fromParameters(
                    dockerHostAddressPoolBase,
                    network,
                    ADDRESS_POOL_DEFAULT_GATEWAY,
                    ADDRESS_POOL_DEFAULT_MASK_LENGTH);
            network++;
        } while (assignedAddressPools.containsKey(candidateAddressPool));
        assignedAddressPools.put(candidateAddressPool, serviceName);
        return candidateAddressPool;
    }

    public void removeAddressPoolAssignment(ContainerNetworkIpamSpec addressPool) {
        assignedAddressPools.remove(addressPool);
    }

    public ContainerNetworkIpamSpec getAssignedAddressPool(String serviceName) throws MappingNotFoundException {
        Map.Entry<ContainerNetworkIpamSpec, String> foundMapping = assignedAddressPools.entrySet().stream()
                .filter(entry -> entry.getValue().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new MappingNotFoundException("No address pool found for service " + serviceName));
        return foundMapping.getKey();
    }

    public class MappingNotFoundException extends Exception {
        public MappingNotFoundException(String message) {
            super(message);
        }
    }

}
