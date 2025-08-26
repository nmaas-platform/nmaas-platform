package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultKServiceOperationsManager implements KServiceOperationsManager {

    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesRepositoryManager repositoryManager;
    private final KubernetesApiClientService kubernetesApiClientService;

    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) {
        throw new NotImplementedException("Service restart is currently not supported.");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void scaleService(Identifier deploymentId, int replicas) {
        final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        final String namespace = namespaceService.namespace(serviceInfo.getDomain());
        final String kubernetesReleaseName = serviceInfo.getDescriptiveDeploymentId().getValue();
        final KCluster remoteCluster = serviceInfo.getRemoteCluster();
        kubernetesApiClientService.getDeployments(remoteCluster, namespace, kubernetesReleaseName).forEach(
                d -> kubernetesApiClientService.scaleDeployment(remoteCluster, namespace, d.getMetadata().getName(), replicas)
        );
        kubernetesApiClientService.getStatefulSets(remoteCluster, namespace, kubernetesReleaseName).forEach(
                s -> kubernetesApiClientService.scaleStatefulSet(remoteCluster, namespace, s.getMetadata().getName(), replicas)
        );
    }

}