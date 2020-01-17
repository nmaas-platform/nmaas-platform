package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Map;
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
     * Collection of access methods to the service
     */
    @ElementCollection
    private Set<ServiceAccessMethod> accessMethods;

    public KubernetesNmServiceInfo () {
        super();
    }

    public KubernetesNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, Integer storageSpace, Identifier descriptiveDeploymentId, KubernetesTemplate kubernetesTemplate) {
        super(deploymentId, deploymentName, domain, storageSpace, descriptiveDeploymentId);
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public KubernetesNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, Integer storageSpace, Identifier descriptiveDeploymentId, Map<String, String> additionalParameters, KubernetesTemplate kubernetesTemplate) {
        super(deploymentId, deploymentName, domain, storageSpace, descriptiveDeploymentId, additionalParameters);
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public void addServiceAccessMethod(ServiceAccessMethod serviceAccessMethod) {
        accessMethods.add(serviceAccessMethod);
    }
}
