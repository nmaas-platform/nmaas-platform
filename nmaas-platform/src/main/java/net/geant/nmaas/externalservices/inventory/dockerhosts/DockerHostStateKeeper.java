package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.entities.DockerHostState;
import net.geant.nmaas.externalservices.inventory.dockerhosts.entities.NumberAssignment;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostStateNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.repositories.DockerHostStateRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the assignment and persistence of Docker Host network related resources, namely ports, VLANs and IP addresses.
 */
@Component
public class DockerHostStateKeeper {

    private static final int MIN_ASSIGNABLE_PORT_NUMBER = 1000;
    private static final int MIN_ASSIGNABLE_VLAN_NUMBER = 500;
    private static final int ADDRESS_POOL_MIN_ASSIGNABLE_ADDRESS = 1;
    private static final int ADDRESS_POOL_DEFAULT_GATEWAY = 254;
    private static final int ADDRESS_POOL_DEFAULT_MASK_LENGTH = 24;

    private DockerHostStateRepository stateRepository;

    private DockerHostRepositoryManager dockerHostRepositoryManager;

    @Autowired
    public DockerHostStateKeeper(DockerHostStateRepository stateRepository, DockerHostRepositoryManager dockerHostRepositoryManager){
        this.stateRepository = stateRepository;
        this.dockerHostRepositoryManager = dockerHostRepositoryManager;
    }

    /**
     * Checks {@link DockerHostState} of given Docker Host for currently assigned ports on the host, assigns a new port
     * and returns its number.
     * Currently it is assumed that a given NM service/tool/container will expose only a single public interface
     * that the client should use to access the GUI.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param deploymentId identifier of NM service deployment in the system
     * @return assigned port number
     * @throws DockerHostNotFoundException when trying to add state for Docker Host that doesn't exist
     */
    @Transactional
    public Integer assignPortForContainer(String dockerHostName, Identifier deploymentId) {
        addStateForDockerHostIfAbsent(dockerHostName);
        return assignPort(stateForDockerHost(dockerHostName), deploymentId);
    }

    private Integer assignPort(DockerHostState state, Identifier deploymentId) {
        return assignPorts(1, state, deploymentId).get(0);
    }

    private List<Integer> assignPorts(int number, DockerHostState state, Identifier deploymentId) {
        List<Integer> newAssignedPorts = new ArrayList<>();
        int count = 0;
        int portNumber = MIN_ASSIGNABLE_PORT_NUMBER;
        while(count < number) {
            while(portAlreadyAssigned(state.getPortAssignments(), portNumber))
                portNumber++;
            newAssignedPorts.add(portNumber);
            count++;
            portNumber++;
        }
        newAssignedPorts.forEach(port -> state.getPortAssignments().add(new NumberAssignment(port, deploymentId)));
        stateRepository.save(state);
        return newAssignedPorts;
    }

    private boolean portAlreadyAssigned(List<NumberAssignment> portAssignments, int portNumber) {
        return portAssignments.stream().anyMatch(a -> a.getNumber().equals(portNumber));
    }

    /**
     * Removes port assignments for given deployment.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param deploymentId identifier of NM service deployment in the system
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public void removePortAssignment(String dockerHostName, Identifier deploymentId) {
        removePortAssignment(stateForDockerHost(dockerHostName), deploymentId);
    }

    private void removePortAssignment(DockerHostState state, Identifier deploymentId) {
        List<NumberAssignment> ports = state.getPortAssignments().stream()
                .filter(a -> a.getOwnerId().equals(deploymentId))
                .collect(Collectors.toList());
        ports.forEach(port -> state.getPortAssignments().remove(port));
        stateRepository.save(state);
    }

    /**
     * Retrieves a port already assigned for given deployment.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param deploymentId identifier of NM service deployment in the system
     * @return assigned port number
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public Integer getAssignedPort(String dockerHostName, Identifier deploymentId) {
        if (!getAssignedPorts(stateForDockerHost(dockerHostName), deploymentId).isEmpty())
            return getAssignedPorts(stateForDockerHost(dockerHostName), deploymentId).get(0);
        else
            return null;
    }

    private List<Integer> getAssignedPorts(DockerHostState state, Identifier deploymentId) {
        return state.getPortAssignments().stream()
                .filter(a -> a.getOwnerId().equals(deploymentId))
                .map(NumberAssignment::getNumber)
                .collect(Collectors.toList());
    }

    /**
     * Checks {@link DockerHostState} of given Docker Host for currently assigned VLANs on the host, assigns a new VLAN
     * and returns its number.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @return assigned VLAN number
     * @throws DockerHostNotFoundException when trying to add state for Docker Host that doesn't exist
     */
    @Transactional
    public int assignVlanForNetwork(String dockerHostName, String domain) {
        addStateForDockerHostIfAbsent(dockerHostName);
        return assignVlan(stateForDockerHost(dockerHostName), domain);
    }

    private int assignVlan(DockerHostState state, String domain) {
        int vlanNumber = MIN_ASSIGNABLE_VLAN_NUMBER;
        while(vlanAlreadyAssigned(state.getVlanAssignments(), vlanNumber))
            vlanNumber++;
        state.getVlanAssignments().add(new NumberAssignment(vlanNumber, Identifier.newInstance(domain)));
        stateRepository.save(state);
        return vlanNumber;
    }

    private boolean vlanAlreadyAssigned(List<NumberAssignment> vlanAssignments, int vlanNumber) {
        return vlanAssignments.stream().anyMatch(a -> a.getNumber().equals(vlanNumber));
    }

    /**
     * Removes VLAN assignment for given domain.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public void removeVlanAssignment(String dockerHostName, String domain) {
        removeVlanAssignment(stateForDockerHost(dockerHostName), domain);
    }

    private void removeVlanAssignment(DockerHostState state, String domain) {
        Optional<NumberAssignment> vlan = state.getVlanAssignments().stream()
                .filter(a -> a.getOwnerId().equals(Identifier.newInstance(domain)))
                .findFirst();
        if (vlan.isPresent()) {
            state.getVlanAssignments().remove(vlan.get());
            stateRepository.save(state);
        }
    }

    /**
     * Retrieves a VLAN already assigned for given domain.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @return assigned VLAN number
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public Integer getAssignedVlan(String dockerHostName, String domain) {
        if (stateForDockerHostNotExists(dockerHostName))
            throw new DockerHostStateNotFoundException("State for given Docker Host was not stored before.");
        return getAssignedVlan(stateForDockerHost(dockerHostName), domain);
    }

    private Integer getAssignedVlan(DockerHostState state, String domain) {
        return state.getVlanAssignments().stream()
                .filter(a -> a.getOwnerId().equals(Identifier.newInstance(domain)))
                .map(NumberAssignment::getNumber)
                .findFirst().orElse(null);
    }

    /**
     * Checks {@link DockerHostState} of given Docker Host for currently assigned network addresses on the host,
     * assigns a new address and returns its number.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @return assigned network address
     * @throws DockerHostNotFoundException when trying to add state for Docker Host that doesn't exist
     */
    @Transactional
    public DockerNetworkIpam assignAddressPoolForNetwork(String dockerHostName, String domain) {
        addStateForDockerHostIfAbsent(dockerHostName);
        return assignAddresses(stateForDockerHost(dockerHostName), domain);
    }

    private DockerNetworkIpam assignAddresses(DockerHostState state, String domain) {
        Integer address = ADDRESS_POOL_MIN_ASSIGNABLE_ADDRESS;
        while(addressAlreadyAssigned(state.getAddressAssignments(), address))
            address++;
        state.getAddressAssignments().add(new NumberAssignment(address, Identifier.newInstance(domain)));
        stateRepository.save(state);
        return DockerNetworkIpam.fromParameters(
                state.getDockerHostAddressPoolBase(),
                address,
                ADDRESS_POOL_DEFAULT_GATEWAY,
                ADDRESS_POOL_DEFAULT_MASK_LENGTH);
    }

    private boolean addressAlreadyAssigned(List<NumberAssignment> addressAssignments, Integer address) {
        return addressAssignments.stream().anyMatch(a -> a.getNumber().equals(address));
    }

    /**
     * Removes address assignment for given client/customer.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public void removeAddressPoolAssignment(String dockerHostName, String domain) {
        removeAddressPoolAssignment(stateForDockerHost(dockerHostName), domain);
    }

    private void removeAddressPoolAssignment(DockerHostState state, String domain) {
        Optional<NumberAssignment> vlan = state.getAddressAssignments().stream()
                .filter(a -> a.getOwnerId().equals(Identifier.newInstance(domain)))
                .findFirst();
        if (vlan.isPresent()) {
            state.getAddressAssignments().remove(vlan.get());
            stateRepository.save(state);
        }
    }

    /**
     * Retrieves an address already assigned for given client/customer.
     *
     * @param dockerHostName name identifying a Docker Host
     * @param domain name of the client domain
     * @return assigned network address
     * @throws DockerHostStateNotFoundException if state for provided Docker Host doesn't exist in repository
     */
    @Transactional
    public DockerNetworkIpam getAssignedAddressPool(String dockerHostName, String domain) {
        return getAssignedAddressPool(stateForDockerHost(dockerHostName), domain);
    }

    private DockerNetworkIpam getAssignedAddressPool(DockerHostState state, String domain) {
        return state.getAddressAssignments().stream()
                .filter(a -> a.getOwnerId().equals(Identifier.newInstance(domain)))
                .map(a -> DockerNetworkIpam.fromParameters(
                                state.getDockerHostAddressPoolBase(),
                                a.getNumber(),
                                ADDRESS_POOL_DEFAULT_GATEWAY,
                                ADDRESS_POOL_DEFAULT_MASK_LENGTH))
                .findFirst().orElse(null);
    }

    private void addStateForDockerHostIfAbsent(String dockerHostName) {
        if (stateForDockerHostNotExists(dockerHostName)) {
            String dockerHostBaseDataNetworkAddress =
                    dockerHostRepositoryManager.loadByName(dockerHostName).getBaseDataNetworkAddress().getHostAddress();
            DockerHostState state = new DockerHostState(dockerHostName, dockerHostBaseDataNetworkAddress);
            stateRepository.save(state);
        }
    }

    private boolean stateForDockerHostNotExists(String dockerHostName) {
        return !stateRepository.findByDockerHostName(dockerHostName).isPresent();
    }

    private DockerHostState stateForDockerHost(String dockerHostName) {
        return stateRepository.findByDockerHostName(dockerHostName)
                .orElseThrow(() -> new DockerHostStateNotFoundException(String.format("State for Docker Host %s was not stored before.", dockerHostName)));
    }

    void removeAllAssignments(String dockerHostName) {
        DockerHostState state = stateForDockerHost(dockerHostName);
        state.setPortAssignments(new ArrayList<>());
        state.setVlanAssignments(new ArrayList<>());
        state.setAddressAssignments(new ArrayList<>());
        stateRepository.save(state);
    }

}
