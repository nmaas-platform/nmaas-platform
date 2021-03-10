package net.geant.nmaas.externalservices.inventory.kubernetes;

import lombok.AllArgsConstructor;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API endpoint for retrieving Kubernetes cluster related configuration
 */
@RestController
@RequestMapping(value = "/api/management/kubernetes")
@AllArgsConstructor
public class KubernetesClusterController {

    private KubernetesClusterIngressManager kClusterIngressManager;

    private KubernetesClusterDeploymentManager kClusterDeploymentManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public KClusterView getKubernetesCluster() {
        return new KClusterView(kClusterIngressManager.getKClusterIngressView(), kClusterDeploymentManager.getKClusterDeploymentView());
    }

}
