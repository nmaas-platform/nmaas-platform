package net.geant.nmaas.orchestrators.dockerswarm.service;

import net.geant.nmaas.orchestrators.dockerswarm.NmServiceDockerSwarmSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceDockerSwarmInfo {

    /**
     * Unique name of the service provided by the caller
     */
    private String name;

    /**
     * Identifier of the service assigned by Docker Swarm
     */
    private String id;

    /**
     * Specification of the service provided by the caller
     */
    private NmServiceDockerSwarmSpec spec;

    /**
     * State in which service should be at this point
     */
    private DesiredState state;

    public NmServiceDockerSwarmInfo(String name, NmServiceDockerSwarmSpec spec, DesiredState state) {
        this.name = name;
        this.spec = spec;
        this.state = state;
    }

    public NmServiceDockerSwarmInfo(String name, String id, NmServiceDockerSwarmSpec spec, DesiredState state) {
        this.name = name;
        this.id = id;
        this.spec = spec;
        this.state = state;
    }

    public void updateState(DesiredState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public NmServiceDockerSwarmSpec getSpec() {
        return spec;
    }

    public DesiredState getState() {
        return state;
    }

    public enum DesiredState {
        READY,
        RUNNING,
        STOPPED;
    }
}
