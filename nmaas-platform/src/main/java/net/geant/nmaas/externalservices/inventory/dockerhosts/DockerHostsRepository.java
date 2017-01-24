package net.geant.nmaas.externalservices.inventory.dockerhosts;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DockerHostsRepository {

    private List<DockerHost> dockerHosts = new ArrayList<>();

    {
        dockerHosts.add(new DockerHost("GN4-DOCKER-1", "http://10.134.250.1:2375", "/home/mgmt/nmaasplatform/volumes", true));
        dockerHosts.add(new DockerHost("GN4-DOCKER-2", "http://10.134.250.2:2375", "/home/mgmt/nmaasplatform/volumes", false));
        dockerHosts.add(new DockerHost("GN4-DOCKER-3", "http://10.134.250.3:2375", "/home/mgmt/nmaasplatform/volumes", false));
    }

    public DockerHost loadPreferredDockerHost() throws DockerHostNotFoundException {
        return dockerHosts.stream().filter((host) -> host.isPreferred()).findFirst().orElseThrow(() -> new DockerHostNotFoundException("Did not find Docker host in repository."));
    }

}
