package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.StorageV1beta1Api;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.util.Config;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Intermediates in the communication between {@link KubernetesManager} and the Kubernetes cluster using its REST API.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesApiConnector {

    private static final int MIN_NUMBER_OF_WORKERS_IN_CLUSTER = 3;

    @Autowired
    private KubernetesClusterManager kubernetesClusterManager;

    @Value("${kubernetes.persistence.class}")
    private String kubernetesPersistenceClass;

    /**
     * Initializes Kubernetes REST API client based on values read from properties.
     */
    @PostConstruct
    public void initApiClient() {
        String kubernetesApiUrl = kubernetesClusterManager.getKubernetesApiUrl();
        ApiClient client = Config.fromUrl(kubernetesApiUrl, false);
        Configuration.setDefaultApiClient(client);
    }

    /**
     * Checks if defined requirements are met by the Kubernetes cluster.
     * List of requirements can be easily extended.
     *
     * @throws KubernetesClusterCheckException if requirements are not met
     */
    @Loggable(LogLevel.INFO)
    public void checkClusterStatusAndPrerequisites() throws KubernetesClusterCheckException {
        try {
            atLeastGivenNumberOfWorkers(MIN_NUMBER_OF_WORKERS_IN_CLUSTER);
            isStorageClassDeployed();
        } catch (ApiException e) {
            throw new KubernetesClusterCheckException(e.getMessage());
        }
    }

    private void atLeastGivenNumberOfWorkers(int expectedNumber) throws ApiException, KubernetesClusterCheckException {
        if (getClusterNodes().size() < expectedNumber)
            throw new KubernetesClusterCheckException("Not enough worker nodes in the cluster (" + getClusterNodes().size() + ")");
    }

    private List<V1Node> getClusterNodes() throws ApiException {
        return new CoreV1Api().listNode(null, null, null, null, 3, false).getItems();
    }

    private void isStorageClassDeployed() throws ApiException, KubernetesClusterCheckException {
        new StorageV1beta1Api().listStorageClass(null, null, null, null, 3, false)
                .getItems().stream()
                .filter(sc -> sc.getMetadata().getName().equals(kubernetesPersistenceClass))
                .findAny()
                .orElseThrow(() -> new KubernetesClusterCheckException("Storage class configured in properties is missing in the cluster"));
    }

}
