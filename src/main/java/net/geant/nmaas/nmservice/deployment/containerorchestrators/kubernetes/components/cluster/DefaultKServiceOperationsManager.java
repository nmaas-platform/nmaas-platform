package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.KubernetesClientConfigFactory;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                final Config config = Config.fromKubeconfig(
                        Files.readString(Path.of(serviceInfo.getRemoteCluster().getPathConfigFile()))
                );
                client = new KubernetesClientBuilder().withConfig(config).build();
            } else {
                client = new KubernetesClientConfigFactory().getClient();
            }
            client.apps()
                    .deployments()
                    .inNamespace(namespaceService.namespace(serviceInfo.getDomain()))
                    .withName(serviceInfo.getDeploymentId().getValue())
                    .scale(replicas);
        } catch (IOException e) {
            log.error("IO error with accessing the file {}", e.getMessage());
        }
    }

}