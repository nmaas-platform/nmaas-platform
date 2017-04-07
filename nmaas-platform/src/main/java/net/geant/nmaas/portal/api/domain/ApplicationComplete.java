package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;

public class ApplicationComplete extends Application {

    private DockerServiceTemplate dockerContainerTemplate;

	public DockerServiceTemplate getDockerContainerTemplate() {
		return dockerContainerTemplate;
	}

	public void setDockerContainerTemplate(DockerServiceTemplate dockerContainerTemplate) {
		this.dockerContainerTemplate = dockerContainerTemplate;
	}
}
