package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Network Management Service deployment information for application deployed with Docker Compose.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class DockerComposeNmServiceInfo extends DockerNmServiceInfo {

    /**
     * Docker compose file template for this service.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerComposeFileTemplate dockerComposeFileTemplate;

    /**
     * Complete Docker Compose file used for this service deployment.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerComposeFile dockerComposeFile;

    /**
     * Docker Compose Service (composed of one or several containers) deployed for this service.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private DockerComposeService dockerComposeService;

    public DockerComposeNmServiceInfo () {
        super();
    }

    public DockerComposeNmServiceInfo(Identifier deploymentId, Identifier applicationId, String domain, DockerComposeFileTemplate dockerComposeFileTemplate) {
        super(deploymentId, applicationId, domain);
        this.dockerComposeFileTemplate = dockerComposeFileTemplate;
    }

    public DockerComposeFileTemplate getDockerComposeFileTemplate() {
        return dockerComposeFileTemplate;
    }

    public void setDockerComposeFileTemplate(DockerComposeFileTemplate dockerComposeFileTemplate) {
        this.dockerComposeFileTemplate = dockerComposeFileTemplate;
    }

    public DockerComposeFile getDockerComposeFile() {
        return dockerComposeFile;
    }

    public void setDockerComposeFile(DockerComposeFile dockerComposeFile) {
        this.dockerComposeFile = dockerComposeFile;
    }

    public DockerComposeService getDockerComposeService() {
        return dockerComposeService;
    }

    public void setDockerComposeService(DockerComposeService dockerComposeService) {
        this.dockerComposeService = dockerComposeService;
    }
}
