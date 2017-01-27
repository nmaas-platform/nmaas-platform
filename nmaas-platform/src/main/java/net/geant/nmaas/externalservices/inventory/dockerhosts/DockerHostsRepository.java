package net.geant.nmaas.externalservices.inventory.dockerhosts;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DockerHostsRepository {

    private List<DockerHost> dockerHosts = new ArrayList<>();

    {
        try {
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-1",
                    InetAddress.getByName("10.134.250.1"),
                    2375,
                    InetAddress.getByName("10.134.250.1"),
                    "/home/mgmt/nmaasplatform/volumes",
                    true));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-2",
                    InetAddress.getByName("10.134.250.2"),
                    2375,
                    InetAddress.getByName("10.134.250.2"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-3",
                    InetAddress.getByName("10.134.250.3"),
                    2375,
                    InetAddress.getByName("10.134.250.3"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
        } catch (UnknownHostException e) {
            System.out.println("Was not enable to complete assignment of static list of Docker Hosts");
        }
    }

    public DockerHost loadPreferredDockerHost() throws DockerHostNotFoundException {
        return dockerHosts.stream().filter((host) -> host.isPreferred()).findFirst().orElseThrow(() -> new DockerHostNotFoundException("Did not find Docker host in repository."));
    }

}
