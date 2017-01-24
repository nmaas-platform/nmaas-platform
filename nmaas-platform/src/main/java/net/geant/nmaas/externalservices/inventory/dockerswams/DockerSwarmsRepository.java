package net.geant.nmaas.externalservices.inventory.dockerswams;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DockerSwarmsRepository {

    private List<DockerSwarm> dockerSwarms = new ArrayList<>();

    {
        DockerSwarmManager swarmManager = new DockerSwarmManager("SWARM-1-MANAGER", "http://10.134.250.81:2375");
        dockerSwarms.add(new DockerSwarm("SWARM-1", true, swarmManager, "/home/mgmt/nmaasplatform/volumes"));
    }

    public DockerSwarmManager loadPreferredDockerSwarmManager() throws DockerSwarmNotFoundException {
        return dockerSwarms.stream().filter(swarm -> swarm.isPreferred()).findFirst().orElseThrow(() -> new DockerSwarmNotFoundException("Did not find Docker Swarm in repository.")).getManager();
    }

}
