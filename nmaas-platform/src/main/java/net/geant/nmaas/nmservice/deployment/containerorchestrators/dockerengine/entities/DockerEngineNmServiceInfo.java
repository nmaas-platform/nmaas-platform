package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Network Management Service deployment information for application deployed with plain Docker Engine API.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class DockerEngineNmServiceInfo extends DockerNmServiceInfo {

    /**
     * Docker container template for this service.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private DockerContainerTemplate dockerContainerTemplate;

    public DockerEngineNmServiceInfo () {
        super();
    }

    public DockerEngineNmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId, DockerContainerTemplate dockerContainerTemplate) {
        super(deploymentId, applicationId, clientId);
        this.dockerContainerTemplate = dockerContainerTemplate;
    }

    public DockerContainerTemplate getDockerContainerTemplate() {
        return dockerContainerTemplate;
    }

    public void setDockerContainerTemplate(DockerContainerTemplate dockerContainerTemplate) {
        this.dockerContainerTemplate = dockerContainerTemplate;
    }

}
