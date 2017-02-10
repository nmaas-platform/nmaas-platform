package net.geant.nmaas.externalservices.inventory.dockerswams;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;

public class DockerSwarm implements NmServiceDeploymentHost {

    private final String name;

    private DockerSwarmManager manager;

    private String volumesPath;

    private final boolean preferred;

    public DockerSwarm(String name, boolean preferred, DockerSwarmManager manager, String volumesPath) {
        this.name = name;
        this.preferred = preferred;
        this.manager = manager;
        this.volumesPath = volumesPath;
    }

    public String getName() {
        return name;
    }

    public DockerSwarmManager getManager() {
        return manager;
    }

    public void setManager(DockerSwarmManager manager) {
        this.manager = manager;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public void setVolumesPath(String volumesPath) {
        this.volumesPath = volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }
}
