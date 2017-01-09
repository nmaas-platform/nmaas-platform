package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;

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
     * The command to be run in the image
     */
    private String command;

    /**
     * Arguments to the command.
     */
    private List<String> commandArguments;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    private List<String> environmentVariables;

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
        if (name == null || name.isEmpty())
            return false;
        return true;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<PortForwardingSpec> getPorts() {
        return ports;
    }

    public void setPorts(List<PortForwardingSpec> ports) {
        this.ports = ports;
    }

    public List<String> getCommandArguments() {
        return commandArguments;
    }

    public void setCommandArguments(List<String> commandArguments) {
        this.commandArguments = commandArguments;
    }

    public List<String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }
}
