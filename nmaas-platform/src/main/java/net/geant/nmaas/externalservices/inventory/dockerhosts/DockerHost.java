package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents Docker Host which runs Docker Engine daemon, exposes an Docker Remote API and is available for container deployment.
 * Eventually data contained within this object should be retrieved from remote OSS system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHost implements NmServiceDeploymentHost {

    public static final int MIN_ASSIGNABLE_PORT_NUMBER = 1000;

    public static final int MIN_ASSIGNABLE_VLAN_NUMBER = 500;

    /**
     * Unique name identifying this Docker host.
     */
    private final String name;

    /**
     * Ip address on which Docker Remote API is exposed.
     */
    private final InetAddress apiIpAddress;

    /**
     * Port on which Docker Remote API is exposed.
     */
    private final Integer apiPort;

    /**
     * Public Ip address of the Docker host which will be used by the clients to access services deployed on containers.
     */
    private final InetAddress publicIpAddress;

    /**
     * Name of the interface to which Docker default bridge is attached.
     */
    private final String accessInterfaceName;

    /**
     * Name of the Docker host interface on which network monitoring/management traffic will be exchanged.
     * On this interface dedicated VLANs will be configured during container deployment.
     */
    private final String dataInterfaceName;

    /**
     * Default root directory on the Docker host on which volumes will be created and mounted on deployed containers.
     */
    private final String volumesPath;

    /**
     * Helper flag indicating whether this Docker host should be always preferred in selection for new container deployment.
     */
    private final boolean preferred;

    /**
     * List of ports on the public interface currently assigned for containers deployed on the host.
     */
    private final List<Integer> assignedPorts = new ArrayList<>();

    /**
     * List of numbers of VLANs currently configured on the data interface for containers deployed on the host.
     */
    private final List<Integer> assignedVlans = new ArrayList<>();

    public DockerHost(String name,
                      InetAddress apiIpAddress,
                      Integer apiPort,
                      InetAddress publicIpAddress,
                      String accessInterfaceName,
                      String dataInterfaceName,
                      String volumesPath,
                      boolean preferred) {
        this.name = name;
        this.apiIpAddress = apiIpAddress;
        this.apiPort = apiPort;
        this.publicIpAddress = publicIpAddress;
        this.accessInterfaceName = accessInterfaceName;
        this.dataInterfaceName = dataInterfaceName;
        this.volumesPath = volumesPath;
        this.preferred = preferred;
    }

    /**
     * Constructs the complete URL of Docker Remote API exposed on the Docker host.
     *
     * @return API URL
     */
    public String apiUrl() {
        try {
            return new URL("http", apiIpAddress.getHostAddress(), apiPort, "").toString();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Checks currently assigned ports on the host and returns a list of next available ports.
     *
     * @param number of available ports to be listed
     */
    public List<Integer> getAvailablePorts(int number) {
        List<Integer> availablePorts = new ArrayList<>();
        int count = 0;
        int portNumber = MIN_ASSIGNABLE_PORT_NUMBER;
        while(count < number) {
            while(assignedPorts.contains(portNumber))
                portNumber++;
            count++;
            assignedPorts.add(portNumber);
            availablePorts.add(portNumber);
        }
        return availablePorts;
    }

    /**
     * Checks currently assigned VLAN numbers on the host and returns the next available number
     *
     * @return VLAN number
     */
    public int getAvailableVlanNumber() {
        int vlanNumber = MIN_ASSIGNABLE_VLAN_NUMBER;
        while (assignedVlans.contains(vlanNumber))
            vlanNumber++;
        assignedVlans.add(vlanNumber);
        return vlanNumber;
    }

    public String getName() {
        return name;
    }

    public InetAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    public String getAccessInterfaceName() {
        return accessInterfaceName;
    }

    public String getDataInterfaceName() {
        return dataInterfaceName;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }

}
