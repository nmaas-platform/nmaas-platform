package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
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
    private DockerHostRepository dockerHostRepository;

    private Map<String, DockerHostState> states = new HashMap<>();

    public int assignPort(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDocketHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignPort(serviceName);
    }

    public void removePortAssignment(String dockerHostName, List<Integer> ports) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removePortAssignment(ports);
    }

    public int getAssignedPort(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedPorts(serviceName).get(0);
    }

    public int assignVlan(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDocketHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignVlan(serviceName);
    }

    public void removeVlanAssignment(String dockerHostName, int vlanNumber) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removeVlanAssignment(vlanNumber);
    }

    public int getAssignedVlan(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedVlan(serviceName);
    }

    public ContainerNetworkIpamSpec assignAddressPool(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostInvalidException {
        addStateForDocketHostIfAbsent(dockerHostName);
        return states.get(dockerHostName).assignAddresses(serviceName);
    }

    public void removeAddressPoolAssignment(String dockerHostName, ContainerNetworkIpamSpec addressPool) throws DockerHostNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        states.get(dockerHostName).removeAddressPoolAssignment(addressPool);
    }

    public ContainerNetworkIpamSpec getAssignedAddressPool(String dockerHostName, String serviceName) throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        if (!states.containsKey(dockerHostName))
            throw new DockerHostNotFoundException("State for given Docker Host was not stored before.");
        return states.get(dockerHostName).getAssignedAddressPool(serviceName);
    }

    private void addStateForDocketHostIfAbsent(String dockerHostName) throws DockerHostNotFoundException, DockerHostInvalidException {
        if (!states.containsKey(dockerHostName)) {
            String dockerHostBaseDataNetworkAddress = dockerHostRepository.loadByName(dockerHostName).getBaseDataNetworkAddress().getHostAddress();
            final DockerHostState state = new DockerHostState(dockerHostName, dockerHostBaseDataNetworkAddress);
            states.put(dockerHostName, state);
        }
    }
}
