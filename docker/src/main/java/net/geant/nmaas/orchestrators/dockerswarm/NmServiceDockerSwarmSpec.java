package net.geant.nmaas.orchestrators.dockerswarm;

import net.geant.nmaas.nmservice.NmServiceSpec;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceDockerSwarmSpec implements NmServiceSpec {

    /**
     * System-defined name for the service.
     */
    private String name;

    /**
     * Arguments to the command.
     */
    private List<String> args;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    private List<String> env;

    /**
     * List of exposed ports that this service is accessible on from the outside.
     */
    private List<PortForwardingSpec> ports;

    public NmServiceDockerSwarmSpec(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Boolean verify() {
        return null;
    }
}
