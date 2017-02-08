package net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.container.ContainerPortForwardingSpec;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceSpec;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerEngineContainerTemplate implements NmServiceTemplate {

    /**
     * Name identifying this template (should be related with the name of the NM service/tool)
     */
    private String name;

    /**
     * A string specifying the image name to use for the container
     */
    private String image;

    /**
     * The command to be run in the image
     */
    private String command;

    /**
     * Field indicating if additional commands must be provided in service specification
     */
    private Boolean commandInSpecRequired = false;

    /**
     * List of exposed ports that this service is accessible on from the outside.
     * During container configuration for each port on the list a published port needs to be assigned.
     */
    private List<ContainerPortForwardingSpec> exposedPorts = new ArrayList<>();

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

    public DockerEngineContainerTemplate(String name, String image) {
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

    public List<ContainerPortForwardingSpec> getExposedPorts() {
        return exposedPorts;
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

    public void setExposedPorts(List<ContainerPortForwardingSpec> exposedPorts) {
        this.exposedPorts = exposedPorts;
    }
}