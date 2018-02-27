package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("env_kubernetes")
public class DefaultKServiceOperationsManager implements KServiceOperationsManager {

    private static final String SERVICE_SELECT_OPTION_RELEASE = "release";
    private static final String SERVICE_SELECT_OPTION_RESTART_AWARE = "restartAware";
    private static final String SERVICE_SELECT_VALUE_RESTART_AWARE = "true";

    private KubernetesRepositoryManager repositoryManager;
    private KubernetesClusterManager kubernetesClusterManager;
    private KNamespaceService namespaceService;

    @Autowired
    public DefaultKServiceOperationsManager(KubernetesRepositoryManager repositoryManager, KubernetesClusterManager kubernetesClusterManager, KNamespaceService namespaceService) {
        this.repositoryManager = repositoryManager;
        this.kubernetesClusterManager = kubernetesClusterManager;
        this.namespaceService = namespaceService;
    }

    /**
     * Service restart is done through pod deletion.
     * Pod will be recreated and init containers will be launched.
     *
     * @param deploymentId unique identifier of service deployment
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) throws KServiceManipulationException, InvalidDeploymentIdException {
        Identifier clientId = repositoryManager.loadClientId(deploymentId);
        String namespace = namespaceService.namespace(clientId);
        KubernetesClient client = kubernetesClusterManager.getApiClient();
        Pod pod = retrievePodObject(namespace, client, deploymentId.value());
        client.pods().delete(pod);
    }

    private Pod retrievePodObject(String namespace, KubernetesClient client, String releaseName) throws KServiceManipulationException {
        Map<String, String> labels = new HashMap<>();
        labels.put(SERVICE_SELECT_OPTION_RELEASE, releaseName);
        labels.put(SERVICE_SELECT_OPTION_RESTART_AWARE, SERVICE_SELECT_VALUE_RESTART_AWARE);
        PodList matchingPods = client.pods().inNamespace(namespace).withLabels(labels).list();
        if (matchingPods.getItems().size() == 1) {
            return matchingPods.getItems().get(0);
        } else {
            throw new KServiceManipulationException(
                    "Query for pod to be restarted returned wrong number of results -> " + matchingPods.getItems().size());
        }
    }
}
