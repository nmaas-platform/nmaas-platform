package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerPortForwardingSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerSpec implements NmServiceSpec {
    /**
     * System-defined name for the container.
     */
    final private String name;

    /**
     * Template for this service
     */
    final private DockerEngineContainerTemplate template;

    /**
     * Name of the client requesting the service (e.g. username)
     */
    private String clientName;

    /**
     * Name of the organization of the client requesting the service (e.g. company abbreviated name)
     */
    private String clientOrganizationName;

    /**
     * The command to be run in the image
     */
    private String command;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    private List<String> environmentVariables;

    /**
     * List of exposed ports that this service is accessible on from the outside.
     */
    private List<ContainerPortForwardingSpec> ports;

    public DockerContainerSpec(String name, DockerEngineContainerTemplate template) {
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
        if (clientName == null || clientName.isEmpty())
            return false;
        if (clientOrganizationName == null || clientOrganizationName.isEmpty())
            return false;
        return true;
    }

    @Override
    public String uniqueDeploymentName() {
        StringBuilder sb = new StringBuilder();
        sb.append(clientOrganizationName).append("-").append(clientName).append("-").append(name);
        return sb.toString();
    }

    @Override
    public NmServiceTemplate template() {
        return template;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientDetails(String clientName, String clientOrganizationName) {
        this.clientName = clientName;
        this.clientOrganizationName = clientOrganizationName;
    }

    public String getClientOrganizationName() {
        return clientOrganizationName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public List<ContainerPortForwardingSpec> getPorts() {
        return ports;
    }

    public void setPorts(List<ContainerPortForwardingSpec> ports) {
        this.ports = ports;
    }
}