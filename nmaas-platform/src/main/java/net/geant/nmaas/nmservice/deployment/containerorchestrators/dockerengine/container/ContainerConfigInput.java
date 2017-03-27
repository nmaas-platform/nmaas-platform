package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

import java.util.List;

public class ContainerConfigInput {

    public static ContainerConfigInput fromSpec(NmServiceInfo serviceInfo) {
        DockerContainerSpec spec = (DockerContainerSpec) serviceInfo.getSpec();
        DockerEngineContainerTemplate template = (DockerEngineContainerTemplate) spec.template();
        ContainerConfigInput input = new ContainerConfigInput();
        input.setImage(template.getImage());
        input.setCommand(compileCommands(template));
        input.setExposedPort(template.getExposedPort());
        input.setEnv(template.getEnv());
        if (template.getEnvVariablesInSpecRequired())
            input.getEnv().addAll(spec.getEnvironmentVariables());
        input.setContainerVolumes(template.getContainerVolumes());
        input.setUniqueDeploymentName(serviceInfo.getAppDeploymentId());
        return input;
    }

    private static String compileCommands(DockerEngineContainerTemplate template) {
        if (!commandInTemplateProvided(template))
            return null;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(template.getCommand());
        return commandBuilder.toString();
    }

    private static boolean commandInTemplateProvided(DockerEngineContainerTemplate template) {
        return template.getCommand() != null && !template.getCommand().isEmpty();
    }

    private String image;

    private String command;

    private ContainerPortForwardingSpec exposedPort;

    private List<String> env;

    private List<String> containerVolumes;

    private String uniqueDeploymentName;

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

    public ContainerPortForwardingSpec getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(ContainerPortForwardingSpec exposedPort) {
        this.exposedPort = exposedPort;
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

    public String getUniqueDeploymentName() {
        return uniqueDeploymentName;
    }

    public void setUniqueDeploymentName(String uniqueDeploymentName) {
        this.uniqueDeploymentName = uniqueDeploymentName;
    }
}
