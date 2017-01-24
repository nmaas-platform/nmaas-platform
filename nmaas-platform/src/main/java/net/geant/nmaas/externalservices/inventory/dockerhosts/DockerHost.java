package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;

public class DockerHost implements NmServiceDeploymentHost {

    private final String name;

    private final String apiUri;

    private final String volumesPath;

    private final boolean preferred;

    public DockerHost(String name, String apiUri, String volumesPath, boolean preferred) {
        this.name = name;
        this.apiUri = apiUri;
        this.volumesPath = volumesPath;
        this.preferred = preferred;
    }

    public String getName() {
        return name;
    }

    public String getApiUri() {
        return apiUri;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }
}
