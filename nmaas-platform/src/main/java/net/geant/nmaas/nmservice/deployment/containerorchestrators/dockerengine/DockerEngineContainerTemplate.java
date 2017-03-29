package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerPortForwardingSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.orchestration.Identifier;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerEngineContainerTemplate implements NmServiceTemplate {

    /**
     * Identifier of the application to which this template corresponds.
     */
    private Identifier applicationId;

    /**
     * Name identifying this template (should be related with the name of the NM service/tool)
     */
    private String name;

    /**
     * A string specifying the image name to use for the container
     */
    private String image;

    /**
     * The configCommand to be run in the image
     */
    private String command;

    /**
     * Field indicating if additional commands must be provided in service specification
     */
    private Boolean commandInSpecRequired = false;

    /**
     * Port exposed by the service/container. During container configuration for this port a published port needs
     * to be assigned.
     */
    private ContainerPortForwardingSpec exposedPort;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    private List<String> env = new ArrayList<>();

    /**
     * Field indicating if additional environment variables must be provided in service specification
     */
    private Boolean envVariablesInSpecRequired = false;

    /**
     * This field represents all the directories on the container for which remote containerVolumes need to be mounted.
     * During container configuration for each listed volume a directory on the Docker Host needs to be assigned.
     */
    private List<String> containerVolumes = new ArrayList<>();

    public DockerEngineContainerTemplate(Identifier applicationId, String name, String image) {
        this.applicationId = applicationId;
        this.name = name;
        this.image = image;
    }

    @Override
    public Boolean verify() {
        if (name == null || name.isEmpty() || image == null || image.isEmpty())
            return false;
        return true;
    }

    @Override
    public Boolean verifyNmServiceSpec(NmServiceSpec spec) {
        if (spec == null || DockerContainerSpec.class != spec.getClass())
            return false;
        DockerContainerSpec dockerSpec = (DockerContainerSpec) spec;
        if (!dockerSpec.verify())
            return false;
        if (envVariablesInSpecRequired && isEmpty(dockerSpec.getEnvironmentVariables().toArray()))
            return false;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Identifier getApplicationId() {
        return applicationId;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getEnv() {
        return env;
    }

    public Boolean getCommandInSpecRequired() {
        return commandInSpecRequired;
    }

    public Boolean getEnvVariablesInSpecRequired() {
        return envVariablesInSpecRequired;
    }

    public ContainerPortForwardingSpec getExposedPort() {
        return exposedPort;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setCommandInSpecRequired(Boolean commandInSpecRequired) {
        this.commandInSpecRequired = commandInSpecRequired;
    }

    public void setEnv(List<String> env) {
        this.env = env;
    }

    public void setEnvVariablesInSpecRequired(Boolean envVariablesInSpecRequired) {
        this.envVariablesInSpecRequired = envVariablesInSpecRequired;
    }

    public List<String> getContainerVolumes() {
        return containerVolumes;
    }

    public void setContainerVolumes(List<String> containerVolumes) {
        this.containerVolumes = containerVolumes;
    }

    public void setExposedPort(ContainerPortForwardingSpec exposedPort) {
        this.exposedPort = exposedPort;
    }
}