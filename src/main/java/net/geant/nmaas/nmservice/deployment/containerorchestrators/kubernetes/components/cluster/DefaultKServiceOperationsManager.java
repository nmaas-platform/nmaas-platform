package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final String kubernetesDeploymentName =
                Stream.of(serviceInfo.getDescriptiveDeploymentId().getValue(), serviceInfo.getKubernetesTemplate().getMainDeploymentName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("-"));
        final KCluster remoteCluster = serviceInfo.getRemoteCluster();
        if (Objects.nonNull(kubernetesApiClientService.getDeployment(remoteCluster, namespace, kubernetesDeploymentName))) {
            kubernetesApiClientService.scaleDeployment(remoteCluster, namespace, kubernetesDeploymentName, replicas);
            return;
        } else if (Objects.nonNull(kubernetesApiClientService.getStatefulSet(remoteCluster, namespace, kubernetesDeploymentName))) {
            kubernetesApiClientService.scaleStatefulSet(remoteCluster, namespace, kubernetesDeploymentName, replicas);
            return;
        }
        throw new InvalidDeploymentIdException("Could not find either deployment or statefulset with given name " + deploymentId);
    }

}