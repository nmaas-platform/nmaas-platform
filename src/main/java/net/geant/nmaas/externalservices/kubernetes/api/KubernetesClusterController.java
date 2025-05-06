package net.geant.nmaas.externalservices.kubernetes.api;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.externalservices.kubernetes.api.model.KClusterView;
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
@RequiredArgsConstructor
public class KubernetesClusterController {

    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public KClusterView getKubernetesCluster() {
        return new KClusterView(kClusterIngressManager.getKClusterIngressView(), kClusterDeploymentManager.getKClusterDeploymentView());
    }

}