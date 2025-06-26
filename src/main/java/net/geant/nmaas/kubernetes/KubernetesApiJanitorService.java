package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.janitor.JanitorResponseException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesApiJanitorService {

    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesApiClientService kubernetesApiClientService;

    public boolean checkIfNamespaceExists(KCluster kCluster, String namespace) {
        log.info("Checking if namespace {} exists on cluster {}", namespace, kCluster.getId());
        return kubernetesApiClientService.checkIfNamespaceExists(kCluster, namespace);
    }

    public void createNamespace(String namespace, List<KeyValueView> annotations) {
        createNamespace(null, namespace, annotations);
    }

    public void createNamespace(KCluster kCluster, String namespace, List<KeyValueView> annotations) {
        log.info("Creating namespace {} with {} annotations on cluster {}",
                namespace,
                annotations.size(),
                Objects.nonNull(kCluster) ? kCluster.getId() : "LOCAL");
        Map<String, String> annotationsMap = new HashMap<>();
        annotations.forEach(a -> annotationsMap.put(a.getKey(), a.getValue()));
        kubernetesApiClientService.createNamespace(kCluster, namespace, annotationsMap);
    }

    public boolean checkIfReady(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        final Deployment deployment = kubernetesApiClientService.getDeployment(kCluster, deploymentId.value(), namespace);
        if (Objects.nonNull(deployment)) {
            return Objects.equals(deployment.getSpec().getReplicas(), deployment.getStatus().getReadyReplicas());
        } else {
            log.info("Deployment {} not found in namespace {}. Looking for a StatefulSet", deploymentId.value(), namespace);
            final StatefulSet statefulSet = kubernetesApiClientService.getStatefulSet(kCluster, deploymentId.value(), namespace);
            if (Objects.nonNull(statefulSet)) {
                return Objects.equals(statefulSet.getSpec().getReplicas(), statefulSet.getStatus().getReadyReplicas());
            }
            log.info("StatefulSet not found as well");
            throw new JanitorResponseException(
                    String.format("Not able to check application state. No deployment/statefulset with name %s found in namespace %s", deploymentId.value(), namespace)
            );
        }
    }

}