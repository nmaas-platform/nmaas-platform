package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
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
     * Initializes Kubernetes REST API client based on values read from properties.
     */
    public void initApiClient() {
        Config config = new ConfigBuilder().withMasterUrl(kubernetesApiUrl).build();
        client = new DefaultKubernetesClient(config);

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

    @Loggable(LogLevel.INFO)
    public void createOrUpdateIngressObject(String ingressObjectName, String externalUrl, String serviceName, int servicePort)
            throws InternalErrorException {
            initApiClient();
        try {
            ExtensionsV1beta1Api api = new ExtensionsV1beta1Api();
            Optional<V1beta1Ingress> existingIngress = api.listNamespacedIngress(kubernetesDefaultNamespace, null, null, null, null, 3, false)
                    .getItems().stream()
                    .filter(io -> io.getMetadata().getName().equals(ingressObjectName))
                    .findAny();
            V1beta1IngressRule rule = prepareNewRule(externalUrl, serviceName, servicePort);
            V1beta1Ingress ingress;
            if (existingIngress.isPresent()) {
                ingress = existingIngress.get();
                ingress.getSpec().addRulesItem(rule);
                api.replaceNamespacedIngress(ingressObjectName, kubernetesDefaultNamespace, ingress, null);
            } else {
                ingress = new V1beta1Ingress();
                ingress.setMetadata(new V1ObjectMeta().name(ingressObjectName));
                ingress.setSpec(new V1beta1IngressSpec().addRulesItem(rule));
                api.createNamespacedIngress(kubernetesDefaultNamespace, ingress, null);
            }
        } catch (ApiException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    private V1beta1IngressRule prepareNewRule(String externalUrl, String serviceName, int servicePort) {
        V1beta1IngressBackend backend = new V1beta1IngressBackend();
        backend.serviceName(serviceName).servicePort(String.valueOf(servicePort));
        V1beta1HTTPIngressPath path = new V1beta1HTTPIngressPath();
        path.backend(backend).path(DEFAULT_SERVICE_PATH);
        V1beta1HTTPIngressRuleValue http = new V1beta1HTTPIngressRuleValue();
        http.addPathsItem(path);
        V1beta1IngressRule rule = new V1beta1IngressRule();
        rule.host(externalUrl).http(http);
        return rule;
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
