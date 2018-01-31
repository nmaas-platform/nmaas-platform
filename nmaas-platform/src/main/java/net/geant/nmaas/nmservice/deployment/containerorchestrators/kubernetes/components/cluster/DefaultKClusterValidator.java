package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KClusterValidator;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Uses the Kubernetes REST API to verify if configured cluster is in proper state and meets all requirements.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class DefaultKClusterValidator implements KClusterValidator {

    private static final int MIN_NUMBER_OF_WORKERS_IN_CLUSTER = 3;

    private KubernetesClusterManager kubernetesClusterManager;

    private String kubernetesPersistenceClass;

    @Autowired
    public DefaultKClusterValidator(KubernetesClusterManager kubernetesClusterManager) {
        this.kubernetesClusterManager = kubernetesClusterManager;
    }

    /**
     * Checks if defined requirements are met by the Kubernetes cluster.
     * List of requirements can be easily extended.
     *
     * @throws KClusterCheckException if requirements are not met
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void checkClusterStatusAndPrerequisites() throws KClusterCheckException {
        KubernetesClient client = kubernetesClusterManager.getApiClient();
        try {
            atLeastGivenNumberOfWorkers(client, MIN_NUMBER_OF_WORKERS_IN_CLUSTER);
            isStorageClassDeployed(client);
        } catch (KubernetesClientException e) {
            throw new KClusterCheckException("KubernetesClientException " + e.getMessage());
        }
    }

    private void atLeastGivenNumberOfWorkers(KubernetesClient client, int expectedNumber) throws KClusterCheckException, KubernetesClientException {
        if (getClusterNodes(client).size() < expectedNumber)
            throw new KClusterCheckException("Not enough worker nodes in the cluster (" + getClusterNodes(client).size() + ")");
    }

    private List<Node> getClusterNodes(KubernetesClient client) throws KubernetesClientException {
        return client.nodes().list().getItems();
    }

    private void isStorageClassDeployed(KubernetesClient client) throws KClusterCheckException {
        // TODO waiting for new library release with storageClass support
    }

    @Value("${kubernetes.persistence.class}")
    public void setKubernetesPersistenceClass(String kubernetesPersistenceClass) {
        this.kubernetesPersistenceClass = kubernetesPersistenceClass;
    }

}
