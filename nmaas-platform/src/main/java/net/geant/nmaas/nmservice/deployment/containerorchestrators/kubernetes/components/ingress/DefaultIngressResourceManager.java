package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KNamespaceService;
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
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DefaultIngressResourceManager implements IngressResourceManager {

    private static final String NMAAS_INGRESS_RESOURCE_NAME_PREFIX = "nmaas-i-client-";
    private static final String NMAAS_INGRESS_CLASS_NAME_PREFIX = "nmaas-iclass-client-";
    private static final String NMAAS_INGRESS_CLASS_ANNOTATION_PARAM_NAME = "kubernetes.io/ingress.class";
    private static final String NMAAS_DOMAIN_SUFFIX = ".nmaas.geant.net";

    private static final String DEFAULT_SERVICE_PATH = "/";
    private static final String SERVICE_SELECT_OPTION_RELEASE = "release";
    private static final String SERVICE_SELECT_OPTION_ACCESS = "access";
    private static final String SERVICE_SELECT_VALUE_ACCESS_FOR_INGRESS = "external";

    private KubernetesClusterManager kubernetesClusterManager;
    private KNamespaceService namespaceService;

    @Autowired
    public DefaultIngressResourceManager(KubernetesClusterManager kubernetesClusterManager, KNamespaceService namespaceService) {
        this.kubernetesClusterManager = kubernetesClusterManager;
        this.namespaceService = namespaceService;
    }

    /**
     * Creates new ingress resource if one does not exists or updates the existing one by adding an ingress rule for newly
     * deployed service.
     * Note:
     * The service name to be used in ingress rule definition is most cases is not the same as the helm chart release
     * name. In such case special lookup algorithm needs to be applied to retrieve the correct service name from the cluster.
     *
     * @param deploymentId unique identifier of service deployment
     * @param clientId identifier of the client requesting the deployment
     * @throws IngressResourceManipulationException if Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized String createOrUpdateIngressResource(Identifier deploymentId, Identifier clientId) throws IngressResourceManipulationException {
        KubernetesClient client = kubernetesClusterManager.getApiClient();
        String namespace = namespaceService.namespace(clientId);
        String ingressResourceName = ingressResourceName(clientId.value());
        String externalUrl = externalUrl(deploymentId.value(), clientId.value());
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
                        ingressClassName(clientId),
                        externalUrl,
                        serviceName,
                        servicePort);
            } else {
                ingress.getMetadata().setResourceVersion(null);
                IngressRule rule = prepareNewRule(externalUrl, serviceName, servicePort);
                ingress.getSpec().getRules().add(rule);
                deleteIngressResource(client, ingress);
            }
            createIngressResource(client, ingress);
            return externalUrl;
        } catch (KubernetesClientException iee) {
            throw new IngressResourceManipulationException("Problem wih executing command on Kubernetes API -> " + iee.getMessage());
        }
    }

    private String ingressResourceName(String clientId) {
        return NMAAS_INGRESS_RESOURCE_NAME_PREFIX + clientId;
    }

    // TODO fix or replace this extrenalURL string creation
    private String externalUrl(String deploymentId, String clientId) {
        return deploymentId.substring(deploymentId.length() - 12) + "." + "client-" + clientId + NMAAS_DOMAIN_SUFFIX;
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
     * @param deploymentId unique identifier of service deployment
     * @param clientId identifier of the client requesting the deployment
     * @throws IngressResourceManipulationException if ingress object with provided name does not exist in the cluster or Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized void deleteIngressRule(Identifier deploymentId, Identifier clientId) throws IngressResourceManipulationException {
        KubernetesClient client = kubernetesClusterManager.getApiClient();
        String namespace = namespaceService.namespace(clientId);
        String ingressResourceName = ingressResourceName(clientId.value());
        String externalUrl = externalUrl(deploymentId.value(), clientId.value());
        try {
            Ingress ingress = client.extensions().ingresses().inNamespace(namespace).list().getItems()
                    .stream()
                    .filter(i -> i.getMetadata().getName().equals(ingressResourceName))
                    .findFirst()
                    .orElseThrow(
                            () -> new IngressResourceManipulationException("Ingress object with name " + ingressResourceName + " does not exist in the cluster"));
            List<IngressRule> filtered = ingress.getSpec().getRules()
                    .stream()
                    .filter(r -> !r.getHost().equals(externalUrl))
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
     * @param clientId identifier of the client requesting the deployment
     * @throws IngressResourceManipulationException if ingress object with provided name does not exist in the cluster or Kubernetes client throws any exception
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void deleteIngressResource(Identifier clientId) throws IngressResourceManipulationException {
        KubernetesClient client = kubernetesClusterManager.getApiClient();
        String ingressResourceName = ingressResourceName(clientId.value());
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

    private String ingressClassName(Identifier clientId) {
        return NMAAS_INGRESS_CLASS_NAME_PREFIX + clientId;
    }

}
