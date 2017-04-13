package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerSpec implements NmServiceSpec {

    /**
     * System-defined name for the container
     */
    private String name;

    /**
     * Template for this container
     */
    private DockerContainerTemplate template;

    /**
     * Unique identifier of the client requesting container deployment
     */
    private Long clientId;

    /**
     * The command to be run in the image at startup
     * (apart from the one defined in the {@link DockerContainerTemplate})
     */
    private String command;

    /**
     * Port exposed by the service/container for UI access
     * (apart from the one defined in the {@link DockerContainerTemplate})
     */
    private DockerContainerPortForwarding exposedPort;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     * (apart from the ones defined in the {@link DockerContainerTemplate})
     */
    private List<String> envVariables;

    public DockerContainerSpec() { }

    public DockerContainerSpec(String name, DockerContainerTemplate template, Long clientId) {
        this.name = name;
        this.template = template;
        this.clientId = clientId;
    }

    public Boolean verify() {
        if (name == null || name.isEmpty())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DockerContainerSpec{" +
                "name='" + name + '\'' +
                ", template=" + template +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DockerContainerTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DockerContainerTemplate template) {
        this.template = template;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(List<String> envVariables) {
        this.envVariables = envVariables;
    }

    public DockerContainerPortForwarding getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(DockerContainerPortForwarding exposedPort) {
        this.exposedPort = exposedPort;
    }
}
