package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.KubernetesApiClientFactory;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultKServiceOperationsManager implements KServiceOperationsManager {

    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesRepositoryManager repositoryManager;

    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) {
        throw new NotImplementedException("Service restart is currently not supported.");
    }

    @Override
    public void scaleDeployment(Identifier deploymentId, int replicas) {
        KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        try {
            KubernetesClient client;
            if (Objects.nonNull(serviceInfo.getRemoteCluster())) {
                client = KubernetesApiClientFactory.getClient(serviceInfo.getRemoteCluster());
            } else {
                client = new KubernetesApiClientFactory().getClient();
            }
            client.apps()
                    .deployments()
                    .inNamespace(namespaceService.namespace(serviceInfo.getDomain()))
                    .withName(serviceInfo.getDeploymentId().getValue())
                    .scale(replicas);
        } catch (KubernetesClientSetupException e) {
            log.error(e.getMessage());
        }
    }

}