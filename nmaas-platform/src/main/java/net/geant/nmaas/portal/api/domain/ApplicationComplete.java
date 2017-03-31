package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;

public class ApplicationComplete extends Application {

    private DockerContainerTemplate dockerContainerTemplate;

    public DockerContainerTemplate getDockerContainerTemplate() {
        return dockerContainerTemplate;
    }

    public void setDockerContainerTemplate(DockerContainerTemplate dockerContainerTemplate) {
        this.dockerContainerTemplate = dockerContainerTemplate;
    }
}
