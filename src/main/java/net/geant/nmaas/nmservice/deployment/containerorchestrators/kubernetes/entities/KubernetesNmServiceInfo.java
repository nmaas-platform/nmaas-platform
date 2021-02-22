package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Network Management Service deployment information for application deployed on Kubernetes cluster.
 */
@Getter
@Setter
@Entity
public class KubernetesNmServiceInfo extends NmServiceInfo {

    /**
     * Kubernetes template for this service
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

    /**
     * Collection of storage volumes for the service
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ServiceStorageVolume> storageVolumes;

    /**
     * Collection of access methods to the service
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ServiceAccessMethod> accessMethods;

    public KubernetesNmServiceInfo () {
        super();
    }

    public KubernetesNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, Identifier descriptiveDeploymentId) {
        super(deploymentId, deploymentName, domain, descriptiveDeploymentId);
    }

}
