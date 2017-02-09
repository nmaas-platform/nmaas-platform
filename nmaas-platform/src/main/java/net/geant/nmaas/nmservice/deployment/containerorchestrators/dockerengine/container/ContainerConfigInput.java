package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;

import java.util.List;

public class ContainerConfigInput {

    public static ContainerConfigInput fromSpec(DockerContainerSpec spec) {
        DockerEngineContainerTemplate template = (DockerEngineContainerTemplate) spec.template();
        ContainerConfigInput input = new ContainerConfigInput();
        input.setImage(template.getImage());
        if (template.getCommandInSpecRequired())
            input.setCommand(spec.getCommand());
        else
            input.setCommand(template.getCommand());
        input.setExposedPorts(template.getExposedPorts());
        input.setEnv(template.getEnv());
        if (template.getEnvVariablesInSpecRequired())
            input.getEnv().addAll(spec.getEnvironmentVariables());
        input.setContainerVolumes(template.getContainerVolumes());
        return input;
    }

    private String image;

    private String command;

    private List<ContainerPortForwardingSpec> exposedPorts;

    private List<String> env;

    private List<String> containerVolumes;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<ContainerPortForwardingSpec> getExposedPorts() {
        return exposedPorts;
    }

    public void setExposedPorts(List<ContainerPortForwardingSpec> exposedPorts) {
        this.exposedPorts = exposedPorts;
    }

    public List<String> getEnv() {
        return env;
    }

    public void setEnv(List<String> env) {
        this.env = env;
    }

    public List<String> getContainerVolumes() {
        return containerVolumes;
    }

    public void setContainerVolumes(List<String> containerVolumes) {
        this.containerVolumes = containerVolumes;
    }
}
