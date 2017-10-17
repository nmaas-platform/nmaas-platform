package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DockerNmServiceInfo extends NmServiceInfo {

    /**
     * Target deployment Docker Host on which this service will be or was deployed.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private DockerHost host;

    public DockerNmServiceInfo() {
        super();
    }

    public DockerNmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId) {
        super(deploymentId, applicationId, clientId);
    }

    public DockerHost getHost() {
        return host;
    }

    public void setHost(DockerHost host) {
        this.host = host;
    }

}
