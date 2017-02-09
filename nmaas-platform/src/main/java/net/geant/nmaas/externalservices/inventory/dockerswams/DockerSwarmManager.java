package net.geant.nmaas.externalservices.inventory.dockerswams;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;

public class DockerSwarmManager implements NmServiceDeploymentHost {

    private final String name;

    private final String apiUri;

    public DockerSwarmManager(String name, String apiUri) {
        this.name = name;
        this.apiUri = apiUri;
    }

    public String getName() {
        return name;
    }

    public String getApiUri() {
        return apiUri;
    }

}
