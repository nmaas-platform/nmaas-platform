package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DockerHost implements NmServiceDeploymentHost {

    public static final int MIN_ASSIGNABLE_PORT_NUMBER = 1000;

    private final String name;

    private final InetAddress apiIpAddress;

    private final Integer apiPort;

    private final InetAddress publicIpAddress;

    private final String volumesPath;

    private final boolean preferred;

    private final List<Integer> assignedPorts = new ArrayList<>();

    public DockerHost(String name, InetAddress apiIpAddress, Integer apiPort, InetAddress publicIpAddress, String volumesPath, boolean preferred) {
        this.name = name;
        this.apiIpAddress = apiIpAddress;
        this.apiPort = apiPort;
        this.publicIpAddress = publicIpAddress;
        this.volumesPath = volumesPath;
        this.preferred = preferred;
    }

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

    public String getName() {
        return name;
    }

    public InetAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }
}
