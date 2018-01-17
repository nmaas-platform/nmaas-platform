package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.*;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.InternalErrorException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
    private static final String DEFAULT_SERVICE_PATH = "/";

    private KubernetesClusterManager kubernetesClusterManager;

    private String kubernetesPersistenceClass;
    private String kubernetesDefaultNamespace;
    private KubernetesClient client;

    @Autowired
    public KubernetesApiConnector(KubernetesClusterManager kubernetesClusterManager) {
        this.kubernetesClusterManager = kubernetesClusterManager;
    }

    /**
     * Checks if defined requirements are met by the Kubernetes cluster.
     * List of requirements can be easily extended.
     *
     * @throws KubernetesClusterCheckException if requirements are not met
     */
    @Loggable(LogLevel.INFO)
    public void checkClusterStatusAndPrerequisites() throws KubernetesClusterCheckException {
        initApiClient();
        try {
            atLeastGivenNumberOfWorkers(MIN_NUMBER_OF_WORKERS_IN_CLUSTER);
            isStorageClassDeployed();
        } catch (KubernetesClientException e) {
            throw new KubernetesClusterCheckException("KubernetesClientException " + e.getMessage());
        }
    }

    private void atLeastGivenNumberOfWorkers(int expectedNumber) throws KubernetesClusterCheckException, KubernetesClientException {
        if (getClusterNodes().size() < expectedNumber)
            throw new KubernetesClusterCheckException("Not enough worker nodes in the cluster (" + getClusterNodes().size() + ")");
    }

    private List<Node> getClusterNodes() throws KubernetesClientException {
        return client.nodes().list().getItems();
    }

    private void isStorageClassDeployed() throws KubernetesClusterCheckException {
        // TODO waiting for new library release with storageClass support
    }

    @Loggable(LogLevel.INFO)
    public void createOrUpdateIngressObject(String ingressObjectName, String externalUrl, String serviceName, int servicePort)
            throws InternalErrorException {
            initApiClient();
        try {

        } catch (KubernetesClientException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Initializes Kubernetes REST API client based on values read from properties.
     */
    public void initApiClient() {
        if (client == null) {
            String kubernetesApiUrl = kubernetesClusterManager.getKubernetesApiUrl();
            Config config = new ConfigBuilder().withMasterUrl(kubernetesApiUrl).build();
            client = new DefaultKubernetesClient(config);
        }
    }

    @Value("${kubernetes.persistence.class}")
    public void setKubernetesPersistenceClass(String kubernetesPersistenceClass) {
        this.kubernetesPersistenceClass = kubernetesPersistenceClass;
    }

    @Value("${kubernetes.namespace}")
    public void setKubernetesDefaultNamespace(String kubernetesDefaultNamespace) {
        this.kubernetesDefaultNamespace = kubernetesDefaultNamespace;
    }

}
