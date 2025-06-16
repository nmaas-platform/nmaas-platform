package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesApiJanitorService {

    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesApiClientService kubernetesApiClientService;

    public boolean checkIfReady(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        final Deployment deployment = kubernetesApiClientService.getDeployment(kCluster, deploymentId.value(), namespace);
        if (Objects.nonNull(deployment)) {
            return Objects.equals(deployment.getSpec().getReplicas(), deployment.getStatus().getReadyReplicas());
        }
        log.info("Deployment {} not found in namespace {}", deploymentId.value(), namespace);
        return false;
    }

}