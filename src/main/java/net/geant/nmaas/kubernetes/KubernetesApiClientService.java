package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.configuration.ConfigFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesApiClientService {

    private final KubernetesApiClientFactory kubernetesApiClientFactory;

    public String getKubernetesVersion(KCluster kCluster) {
        try (KubernetesClient client = initClient(kCluster)) {
            return String.join("-", client.getKubernetesVersion().getMajor(), client.getKubernetesVersion().getMinor());
        }
    }

    public boolean checkIfNamespaceExists(KCluster kCluster, String namespace) {
        try (KubernetesClient client = initClient(kCluster)) {
            return client.namespaces().withName(namespace).get() != null;
        }
    }

    public void createNamespace(KCluster kCluster, String namespace, Map<String, String> annotations) {
        try (KubernetesClient client = initClient(kCluster)) {
            Namespace ns = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespace)
                    .addToLabels("name", namespace)
                    .addToAnnotations(annotations)
                    .endMetadata()
                    .build();
            client.namespaces().resource(ns).create();
        }
    }

    public Deployment getDeployment(KCluster kCluster, String namespace, String deploymentName) {
        try (KubernetesClient client = initClient(kCluster)) {
            NonNamespaceOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> deploymentsInNamespace = client.apps()
                    .deployments()
                    .inNamespace(namespace);
            return deploymentsInNamespace
                    .withName(deploymentName)
                    .get();
        }
    }

    public List<Deployment> getDeployments(KCluster kCluster, String namespace, String releaseName) {
        try (KubernetesClient client = initClient(kCluster)) {
            NonNamespaceOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> deploymentsInNamespace = client.apps()
                    .deployments()
                    .inNamespace(namespace);
            return deploymentsInNamespace.list().getItems().stream()
                    .filter(d -> d.getMetadata().getName().equalsIgnoreCase(releaseName) || d.getMetadata().getName().startsWith(releaseName + "-"))
                    .toList();
        }
    }

    public StatefulSet getStatefulSet(KCluster kCluster, String namespace, String statefulSetName) {
        try (KubernetesClient client = initClient(kCluster)) {
            NonNamespaceOperation<StatefulSet, StatefulSetList, RollableScalableResource<StatefulSet>> statefulSetsInNamespace = client.apps()
                    .statefulSets()
                    .inNamespace(namespace);
            return statefulSetsInNamespace
                    .withName(statefulSetName)
                    .get();
        }
    }

    public List<StatefulSet> getStatefulSets(KCluster kCluster, String namespace, String releaseName) {
        try (KubernetesClient client = initClient(kCluster)) {
            NonNamespaceOperation<StatefulSet, StatefulSetList, RollableScalableResource<StatefulSet>> statefulSetsInNamespace = client.apps()
                    .statefulSets()
                    .inNamespace(namespace);
            return statefulSetsInNamespace.list().getItems().stream()
                    .filter(d -> d.getMetadata().getName().equalsIgnoreCase(releaseName) || d.getMetadata().getName().startsWith(releaseName + "-"))
                    .toList();
        }
    }

    public void scaleDeployment(KCluster kCluster, String namespace, String deploymentName, int replicas) {
        try (KubernetesClient client = initClient(kCluster)) {
            client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .scale(replicas);
        }
    }

    public void scaleStatefulSet(KCluster kCluster, String namespace, String deploymentName, int replicas) {
        try (KubernetesClient client = initClient(kCluster)) {
            client.apps()
                    .statefulSets()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .scale(replicas);
        }
    }

    public Service getService(KCluster kCluster, String namespace, String serviceName) {
        try (KubernetesClient client = initClient(kCluster)) {
            return client.services()
                    .inNamespace(namespace)
                    .withName(serviceName)
                    .get();
        }
    }

    public PodList getPods(KCluster kCluster, String namespace) {
        try (KubernetesClient client = initClient(kCluster)) {
            return client.pods()
                    .inNamespace(namespace)
                    .list();
        }
    }

    public String getLogs(KCluster kCluster, String namespace, String podName, String containerName, int limit) {
        try (KubernetesClient client = initClient(kCluster)) {
            var containerResource = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName);
            if (limit > 0) {
                return containerResource.tailingLines(limit).getLog(true);
            }
            return containerResource.getLog(true);
        }
    }

    public void createOrReplaceConfigMap(KCluster kCluster, String namespace, String configMapName, List<ConfigFile> configFiles) {
        Validate.isTrue(!StringUtils.isBlank(configMapName), "Desired config map name is missing");
        Validate.isTrue(!configFiles.isEmpty(), "Provided list of configuration files is empty");
        try (KubernetesClient client = initClient(kCluster)) {
            Map<String, String> configFilesWithNames = new HashMap<>();
            configFiles.forEach(configFile -> configFilesWithNames.put(configFile.getFileName(), configFile.getFileContent()));
            ConfigMap configMap = new ConfigMapBuilder()
                    .withNewMetadata()
                    .withName(configMapName)
                    .withNamespace(namespace)
                    .endMetadata()
                    .withData(configFilesWithNames)
                    .build();
            if (client.configMaps().inNamespace(namespace).withName(configMapName).get() == null) {
                client.configMaps()
                        .inNamespace(namespace)
                        .resource(configMap)
                        .create();
            } else {
                client.configMaps()
                        .inNamespace(namespace)
                        .resource(configMap)
                        .update();
            }
        }
    }

    public void deleteConfigMapIfExists(KCluster kCluster, String namespace, String configMapNamePrefix) {
        Validate.isTrue(!StringUtils.isBlank(configMapNamePrefix));
        try (KubernetesClient client = initClient(kCluster)) {
            List<ConfigMap> configMaps = client.configMaps().inNamespace(namespace).list().getItems();
            configMaps.stream()
                    .map(configMap -> configMap.getMetadata().getName())
                    .filter(configMapName -> configMapName.equalsIgnoreCase(configMapNamePrefix) || configMapName.startsWith(configMapNamePrefix + "-"))
                    .forEach(configMapName ->
                            client.configMaps().inNamespace(namespace).withName(configMapName).delete()
                    );
        }
    }

    public void createOrReplaceBasicAuth(KCluster kCluster, String namespace, String secretName, String basicAuthCredentials) {
        Validate.isTrue(!StringUtils.isBlank(secretName));
        Validate.isTrue(!StringUtils.isBlank(basicAuthCredentials));
        try (KubernetesClient client = initClient(kCluster)) {
            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName(secretName)
                    .withNamespace(namespace)
                    .endMetadata()
                    .withData(Map.of("auth", basicAuthCredentials))
                    .build();
            client.secrets()
                    .inNamespace(namespace)
                    .resource(secret)
                    .create();
        }
    }

    public void deleteSecretIfExists(KCluster kCluster, String namespace, String secretName) {
        Validate.isTrue(!StringUtils.isBlank(secretName));
        try (KubernetesClient client = initClient(kCluster)) {
            client.secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .delete();
        }
    }

    public byte[] readClusterConfigBytesFromSecret(String namespace, String secretName) {
        try (KubernetesClient client = initClient(null)) {
            Secret secret = client.secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .get();
            if (secret == null) {
                throw new NoSuchElementException(String.format("Secret %s not found namespace %s", secretName, namespace));
            }

            String encoded = secret.getData().get("value");
            if (encoded == null) {
                throw new NoSuchElementException("Expected field not found in secret");
            }

            // The content is Base64-encoded by Kubernetes, so decode it
            return Base64.getDecoder().decode(encoded);
        }
    }

    private KubernetesClient initClient(KCluster kCluster) {
        KubernetesClient client;
        if (Objects.nonNull(kCluster)) {
            client = KubernetesApiClientFactory.getClient(kCluster);
        } else {
            client = kubernetesApiClientFactory.getClient();
        }
        return client;
    }

    public KubernetesClient getDirectClient(KCluster kCluster) {
        return initClient(kCluster);
    }

}
