package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service.PortForwardingSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

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
     * The configCommand to be run in the image
     */
    private String command;

    /**
     * Arguments to the configCommand.
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

    public DockerSwarmServiceSpec() { }

    public DockerSwarmServiceSpec(String name, DockerSwarmNmServiceTemplate template) {
        this.name = name;
        this.template = template;
    }

    public Boolean verify() {
        if (name == null || name.isEmpty())
            return false;
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DockerSwarmNmServiceTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DockerSwarmNmServiceTemplate template) {
        this.template = template;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
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

    public List<PortForwardingSpec> getPorts() {
        return ports;
    }

    public void setPorts(List<PortForwardingSpec> ports) {
        this.ports = ports;
    }
}
