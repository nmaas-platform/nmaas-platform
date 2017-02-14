package net.geant.nmaas.externalservices.inventory.dockerhosts;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores a static list of Docker Hosts available in the system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerHostRepository {

    public static final String ANSIBLE_DOCKER_HOST_NAME = "GN4-ANSIBLE-HOST";

    private List<DockerHost> dockerHosts = new ArrayList<>();

    {
        try {
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-1",
                    InetAddress.getByName("10.134.250.1"),
                    2375,
                    InetAddress.getByName("10.134.250.1"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.11.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    true));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-2",
                    InetAddress.getByName("10.134.250.2"),
                    2375,
                    InetAddress.getByName("10.134.250.2"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.12.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-3",
                    InetAddress.getByName("10.134.250.3"),
                    2375,
                    InetAddress.getByName("10.134.250.3"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.13.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            dockerHosts.add(new DockerHost(
                    ANSIBLE_DOCKER_HOST_NAME,
                    InetAddress.getByName("10.134.250.6"),
                    2375,
                    InetAddress.getByName("10.134.250.6"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.16.0.0"),
                    "/home/mgmt/ansible/volumes",
                    false));
        } catch (UnknownHostException e) {
            System.out.println("Was not enable to complete assignment of static list of Docker Hosts");
        }
    }

    public DockerHost loadByName(String hostName) throws DockerHostNotFoundException {
        return dockerHosts.stream()
                .filter((host) -> host.getName().equals(hostName))
                .findFirst()
                .orElseThrow(() -> new DockerHostNotFoundException("Did not find host with name " + hostName + " in repository"));
    }

    public DockerHost loadPreferredDockerHost() throws DockerHostNotFoundException {
        return dockerHosts.stream()
                .filter((host) -> host.isPreferred())
                .findFirst()
                .orElseThrow(() -> new DockerHostNotFoundException("Did not find Docker host in repository."));
    }

}
