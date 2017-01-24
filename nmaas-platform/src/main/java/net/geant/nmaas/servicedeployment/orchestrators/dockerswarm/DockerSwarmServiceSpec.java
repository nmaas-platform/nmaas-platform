package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.PortForwardingSpec;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerSwarmServiceSpec implements NmServiceSpec {

    /**
     * System-defined name for the service.
     */
    private String name;

    /**
     * Service template
     */
    private DockerSwarmNmServiceTemplate template;

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

    public DockerSwarmServiceSpec(String name, DockerSwarmNmServiceTemplate template) {
        this.name = name;
        this.template = template;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Boolean verify() {
        if (name == null || name.isEmpty())
            return false;
        return true;
    }

    @Override
    public String uniqueDeploymentName() {
        // TODO should be replaced with something more sophisticated
        return name;
    }

    @Override
    public NmServiceTemplate template() {
        return null;
    }

    public DockerSwarmNmServiceTemplate getTemplate() {
        return template;
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
