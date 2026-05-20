package net.geant.nmaas.kubernetes.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.kubernetes.KClusterDto;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.KubernetesClusterIngressManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API endpoint for retrieving Kubernetes cluster-related configuration
 */
@RestController
@RequestMapping(value = "/api/${nmaas.api.version:v1}/management/kubernetes")
@RequiredArgsConstructor
@Tag(name = "Kubernetes", description = "Kubernetes cluster management API")
public class KubernetesClusterController {

    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public KClusterDto getKubernetesCluster() {
        return new KClusterDto(kClusterIngressManager.getKClusterIngressView(), kClusterDeploymentManager.getKClusterDeploymentView());
    }

}
