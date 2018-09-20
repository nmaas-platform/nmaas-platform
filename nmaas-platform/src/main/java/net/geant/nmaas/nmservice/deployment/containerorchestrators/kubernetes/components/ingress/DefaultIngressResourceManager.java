package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterApiManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Uses the Kubernetes REST API to create, update and delete ingress resource.
 */
@Component
public class DefaultIngressResourceManager implements IngressResourceManager {

    private static final String NMAAS_INGRESS_RESOURCE_NAME_PREFIX = "nmaas-i-";
    private static final String NMAAS_INGRESS_CLASS_NAME_PREFIX = "nmaas-iclass-";
    private static final String NMAAS_INGRESS_CLASS_ANNOTATION_PARAM_NAME = "kubernetes.io/ingress.class";

    private static final String DEFAULT_SERVICE_PATH = "/";
    private static final String SERVICE_SELECT_OPTION_RELEASE = "release";
    private static final String SERVICE_SELECT_OPTION_ACCESS = "access";
    private static final String SERVICE_SELECT_VALUE_ACCESS_FOR_INGRESS = "external";

    private KClusterApiManager clusterApiManager;
    private KNamespaceService namespaceService;

    @Autowired
    public DefaultIngressResourceManager(KClusterApiManager clusterApiManager, KNamespaceService namespaceService) {
        this.clusterApiManager = clusterApiManager;
        this.namespaceService = namespaceService;
    }

    /**
     * Generates URL to be used to access the deployed service from outside of the cluster.
     *
     * @param domain name of the client domain for this deployment
     * @param deploymentName name of the deployment provided by the user
     * @param externalServiceDomain base domain name for external services
     * @return URL under which deployed service is available
     */
    @Override
    public String generateServiceExternalURL(String domain, String deploymentName, String externalServiceDomain) {
        return externalUrl(deploymentName, domain, externalServiceDomain);
    }

    /**
     * Creates new ingress resource if one does not exists or updates the existing one by adding an ingress rule for newly
     * deployed service.
     * Note:
     * The service name to be used in ingress rule definition is most cases is not the same as the helm chart release
     * name. In such case special lookup algorithm needs to be applied to retrieve the correct service name from the cluster.
     *
     * @param deploymentId unique identifier of service deployment
     * @param domain name of the client domain for this deployment
     * @param serviceExternalUrl service external URL
     * @throws IngressResourceManipulationException if Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized void createOrUpdateIngressResource(Identifier deploymentId, String domain, String serviceExternalUrl)
            throws IngressResourceManipulationException {
        KubernetesClient client = clusterApiManager.getApiClient();
        String namespace = namespaceService.namespace(domain);
        String ingressResourceName = ingressResourceName(domain);
        String releaseName = deploymentId.value();
        Service serviceObject = retrieveServiceObject(namespace, client, releaseName);
        String serviceName = extractServiceName(serviceObject);
        int servicePort = extractServicePort(serviceObject);
        try {
            Ingress ingress = client.extensions().ingresses().inNamespace(namespace).list().getItems()
                    .stream()
                    .filter(i -> i.getMetadata().getName().equals(ingressResourceName))
                    .findFirst()
                    .orElse(null);
            if(ingress == null) {
                ingress = prepareNewIngress(
                        namespace,
                        ingressResourceName,
                        ingressClassName(domain),
                        serviceExternalUrl,
                        serviceName,
                        servicePort);
            } else {
                ingress.getMetadata().setResourceVersion(null);
                IngressRule rule = prepareNewRule(serviceExternalUrl, serviceName, servicePort);
                ingress.getSpec().getRules().add(rule);
                deleteIngressResource(client, ingress);
            }
            createIngressResource(client, ingress);
        } catch (KubernetesClientException iee) {
            throw new IngressResourceManipulationException("Problem wih executing command on Kubernetes API -> " + iee.getMessage());
        }
    }

    private String ingressResourceName(String domain) {
        return NMAAS_INGRESS_RESOURCE_NAME_PREFIX + domain.toLowerCase();
    }

    private String externalUrl(String deploymentName, String domain, String externalServiceDomain) {
        return deploymentName + "." + domain.toLowerCase() + "." + externalServiceDomain;
    }

    private String ingressClassName(String domain) {
        return NMAAS_INGRESS_CLASS_NAME_PREFIX + domain.toLowerCase();
    }

    private Service retrieveServiceObject(String namespace, KubernetesClient client, String releaseName) throws IngressResourceManipulationException {
        Map<String, String> labels = new HashMap<>();
        labels.put(SERVICE_SELECT_OPTION_RELEASE, releaseName);
        labels.put(SERVICE_SELECT_OPTION_ACCESS, SERVICE_SELECT_VALUE_ACCESS_FOR_INGRESS);
        ServiceList matchingServices = client.services().inNamespace(namespace).withLabels(labels).list();
        if (matchingServices.getItems().size() == 1) {
            return matchingServices.getItems().get(0);
        } else {
            throw new IngressResourceManipulationException(
                    "Query for service to be included in Ingress resources returned wrong number of results -> " + matchingServices.getItems().size());
        }
    }

    private String extractServiceName(Service serviceObject) {
        return serviceObject.getMetadata().getName();
    }

    // it is assumed that only one service port exists
    private int extractServicePort(Service serviceObject) {
        return serviceObject.getSpec().getPorts().get(0).getPort();
    }

    private void createIngressResource(KubernetesClient client, Ingress ingress) {
        client.extensions().ingresses().create(ingress);
    }

    private void deleteIngressResource(KubernetesClient client, Ingress ingress) {
        client.extensions().ingresses().delete(ingress);
    }

    /**
     * Removes ingress rule from an existing ingress resource.
     *
     * @param externalServiceUrl external URL assigned for the deployment
     * @param domain name of the domain for this deployment
     * @throws IngressResourceManipulationException if ingress object with provided name does not exist in the cluster or Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized void deleteIngressRule(String externalServiceUrl, String domain) throws IngressResourceManipulationException {
        KubernetesClient client = clusterApiManager.getApiClient();
        String namespace = namespaceService.namespace(domain);
        String ingressResourceName = ingressResourceName(domain);
        try {
            Ingress ingress = client.extensions().ingresses().inNamespace(namespace).list().getItems()
                    .stream()
                    .filter(i -> i.getMetadata().getName().equals(ingressResourceName))
                    .findFirst()
                    .orElseThrow(
                            () -> new IngressResourceManipulationException("Ingress object with name " + ingressResourceName + " does not exist in the cluster"));
            List<IngressRule> filtered = ingress.getSpec().getRules()
                    .stream()
                    .filter(r -> !r.getHost().equals(externalServiceUrl))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                deleteIngressResource(client, ingress);
            } else {
                ingress.getMetadata().setResourceVersion(null);
                ingress.getSpec().setRules(filtered);
                deleteIngressResource(client, ingress);
                createIngressResource(client, ingress);
            }
        } catch (KubernetesClientException e) {
            throw new IngressResourceManipulationException(e.getMessage());
        }
    }

    /**
     * Removes ingress resource for given client.
     *
     * @param domain name of the domain for this deployment
     * @throws IngressResourceManipulationException if ingress object with provided name does not exist in the cluster or Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void deleteIngressResource(String domain) throws IngressResourceManipulationException {
        KubernetesClient client = clusterApiManager.getApiClient();
        String ingressResourceName = ingressResourceName(domain);
        try {
            Ingress ingress = client.extensions().ingresses().list().getItems()
                    .stream()
                    .filter(i -> i.getMetadata().getName().equals(ingressResourceName))
                    .findFirst()
                    .orElseThrow(
                            () -> new IngressResourceManipulationException("Ingress object with name " + ingressResourceName + " does not exist in the cluster"));
            deleteIngressResource(client, ingress);
        } catch (KubernetesClientException e) {
            throw new IngressResourceManipulationException(e.getMessage());
        }
    }

    private Ingress prepareNewIngress(String namespace, String ingressObjectName, String ingressClassName, String externalUrl, String serviceName, int servicePort) {
        ObjectMeta metadata = prepareMetadata(namespace, ingressObjectName, ingressClassName);
        IngressRule rule = prepareNewRule(externalUrl, serviceName, servicePort);
        IngressSpec ingressSpec = new IngressSpec(null, Arrays.asList(rule), null);
        return new Ingress(null, null, metadata, ingressSpec, null);
    }

    private ObjectMeta prepareMetadata(String namespace, String ingressObjectName, String ingressClassName) {
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(ingressObjectName);
        metadata.setNamespace(namespace);
        Map<String, String> annotations = new HashMap<>();
        annotations.put(NMAAS_INGRESS_CLASS_ANNOTATION_PARAM_NAME, ingressClassName);
        metadata.setAnnotations(annotations);
        return metadata;
    }

    private IngressRule prepareNewRule(String externalUrl, String serviceName, int servicePort) {
        IngressBackend backend = new IngressBackend(serviceName, new IntOrString(servicePort));
        HTTPIngressPath path = new HTTPIngressPath(backend, DEFAULT_SERVICE_PATH);
        HTTPIngressRuleValue ruleValue = new HTTPIngressRuleValue(Arrays.asList(path));
        return new IngressRule(externalUrl, ruleValue);
    }

}
