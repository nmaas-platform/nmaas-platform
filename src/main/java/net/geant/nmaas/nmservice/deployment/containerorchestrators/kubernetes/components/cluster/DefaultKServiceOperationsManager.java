package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClientSetupException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
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
    public void scaleDeployment(Identifier deploymentId, int replicas) throws KubernetesClientSetupException {
        KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        final String namespace = namespaceService.namespace(serviceInfo.getDomain());
        final String kubernetesDeploymentName =
                Stream.of(serviceInfo.getDescriptiveDeploymentId().getValue(), serviceInfo.getKubernetesTemplate().getMainDeploymentName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("-"));
        kubernetesApiClientService.scaleDeployment(serviceInfo.getRemoteCluster(), namespace, kubernetesDeploymentName, replicas);
    }

}