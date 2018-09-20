package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Network Management Service deployment information for application deployed with Docker Compose.
 */
@Getter
@Setter
@Entity
public class DockerComposeNmServiceInfo extends NmServiceInfo {

    /**
     * Target deployment Docker Host on which this service will be or was deployed.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private DockerHost host;

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

    public DockerComposeNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, DockerComposeFileTemplate dockerComposeFileTemplate) {
        super(deploymentId, deploymentName, domain);
        this.dockerComposeFileTemplate = dockerComposeFileTemplate;
    }
}
