package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigInput {

    static ContainerConfigInput fromSpec(NmServiceInfo serviceInfo) {
        DockerContainerTemplate template = serviceInfo.getDockerContainerTemplate();
        ContainerConfigInput input = new ContainerConfigInput();
        input.setImage(template.getImage());
        input.setCommand(compileCommands(template));
        input.setExposedPort(template.getExposedPort());
        input.setEnv(template.getEnvVariables());
        input.setContainerVolumes(template.getContainerVolumes());
        input.setUniqueDeploymentName(serviceInfo.getDeploymentId().value());
        return input;
    }

    private static String compileCommands(DockerContainerTemplate template) {
        if (!commandInTemplateProvided(template))
            return null;
        return template.getCommand();
    }

    private static boolean commandInTemplateProvided(DockerContainerTemplate template) {
        return template.getCommand() != null && !template.getCommand().isEmpty();
    }

    private String image;

    private String command;

    private DockerContainerPortForwarding exposedPort;

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

    DockerContainerPortForwarding getExposedPort() {
        return exposedPort;
    }

    void setExposedPort(DockerContainerPortForwarding exposedPort) {
        this.exposedPort = exposedPort;
    }

    List<String> getEnv() {
        return env;
    }

    void setEnv(List<String> env) {
        this.env = env;
    }

    List<String> getContainerVolumes() {
        return containerVolumes;
    }

    void setContainerVolumes(List<String> containerVolumes) {
        this.containerVolumes = containerVolumes;
    }

    String getUniqueDeploymentName() {
        return uniqueDeploymentName;
    }

    void setUniqueDeploymentName(String uniqueDeploymentName) {
        this.uniqueDeploymentName = uniqueDeploymentName;
    }
}
