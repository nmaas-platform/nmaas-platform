package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiJanitorService;
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

    private final KubernetesRepositoryManager repositoryManager;
    private final KubernetesApiJanitorService kubernetesApiJanitorService;

    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) {
        throw new NotImplementedException("Service restart is currently not supported.");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void scaleService(Identifier deploymentId, int replicas) {
        final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        kubernetesApiJanitorService.scaleService(serviceInfo.getRemoteCluster(),
                serviceInfo.getDescriptiveDeploymentId(), serviceInfo.getDomain(), replicas);
    }

}
