package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.configuration.ConfigFile;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DummyKubernetesApiClientService extends KubernetesApiClientService {

    private static final PodList EMPTY_POD_LIST = new PodListBuilder().withItems(List.of()).build();

    private final Map<String, Namespace> namespaces = new ConcurrentHashMap<>();
    private final Map<ResourceKey, Deployment> deployments = new ConcurrentHashMap<>();
    private final Map<ResourceKey, StatefulSet> statefulSets = new ConcurrentHashMap<>();
    private final Map<ResourceKey, Service> services = new ConcurrentHashMap<>();
    private final Map<NamespaceKey, PodList> pods = new ConcurrentHashMap<>();
    private final Map<LogKey, String> logs = new ConcurrentHashMap<>();
    private final Map<ResourceKey, ConfigMap> configMaps = new ConcurrentHashMap<>();
    private final Map<ResourceKey, Secret> secrets = new ConcurrentHashMap<>();
    private final Map<ResourceKey, byte[]> clusterConfigSecrets = new ConcurrentHashMap<>();

    private volatile String kubernetesVersion = "0-0";

    public DummyKubernetesApiClientService() {
        super(null);
    }

    public void reset() {
        namespaces.clear();
        deployments.clear();
        statefulSets.clear();
        services.clear();
        pods.clear();
        logs.clear();
        configMaps.clear();
        secrets.clear();
        clusterConfigSecrets.clear();
        kubernetesVersion = "0-0";
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }

    public void putDeployment(KCluster kCluster, String namespace, Deployment deployment) {
        deployments.put(new ResourceKey(clusterKey(kCluster), namespace, deployment.getMetadata().getName()), deployment);
    }

    public void putStatefulSet(KCluster kCluster, String namespace, StatefulSet statefulSet) {
        statefulSets.put(new ResourceKey(clusterKey(kCluster), namespace, statefulSet.getMetadata().getName()), statefulSet);
    }

    public void putService(KCluster kCluster, String namespace, Service service) {
        services.put(new ResourceKey(clusterKey(kCluster), namespace, service.getMetadata().getName()), service);
    }

    public void setPods(KCluster kCluster, String namespace, PodList podList) {
        pods.put(new NamespaceKey(clusterKey(kCluster), namespace), podList);
    }

    public void setLogs(KCluster kCluster, String namespace, String podName, String containerName, String logOutput) {
        logs.put(new LogKey(clusterKey(kCluster), namespace, podName, containerName), logOutput);
    }

    public void putClusterConfigSecret(String namespace, String secretName, byte[] configBytes) {
        clusterConfigSecrets.put(new ResourceKey(ClusterKey.LOCAL.value(), namespace, secretName), Arrays.copyOf(configBytes, configBytes.length));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public String getKubernetesVersion(KCluster kCluster) {
        return kubernetesVersion;
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public boolean checkIfNamespaceExists(KCluster kCluster, String namespace) {
        return namespaces.containsKey(namespaceKey(kCluster, namespace));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void createNamespace(KCluster kCluster, String namespace, Map<String, String> annotations) {
        namespaces.put(namespaceKey(kCluster, namespace), new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .addToAnnotations(annotations)
                .endMetadata()
                .build());
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public Deployment getDeployment(KCluster kCluster, String namespace, String deploymentName) {
        return deployments.get(new ResourceKey(clusterKey(kCluster), namespace, deploymentName));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public List<Deployment> getDeployments(KCluster kCluster, String namespace, String releaseName) {
        return deployments.entrySet().stream()
                .filter(entry -> entry.getKey().cluster().equals(clusterKey(kCluster)))
                .filter(entry -> entry.getKey().namespace().equals(namespace))
                .map(Map.Entry::getValue)
                .filter(deployment -> hasReleaseName(deployment.getMetadata().getName(), releaseName))
                .toList();
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public StatefulSet getStatefulSet(KCluster kCluster, String namespace, String statefulSetName) {
        return statefulSets.get(new ResourceKey(clusterKey(kCluster), namespace, statefulSetName));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public List<StatefulSet> getStatefulSets(KCluster kCluster, String namespace, String releaseName) {
        return statefulSets.entrySet().stream()
                .filter(entry -> entry.getKey().cluster().equals(clusterKey(kCluster)))
                .filter(entry -> entry.getKey().namespace().equals(namespace))
                .map(Map.Entry::getValue)
                .filter(statefulSet -> hasReleaseName(statefulSet.getMetadata().getName(), releaseName))
                .toList();
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void scaleDeployment(KCluster kCluster, String namespace, String deploymentName, int replicas) {
        Deployment deployment = getDeployment(kCluster, namespace, deploymentName);
        if (deployment != null && deployment.getSpec() != null) {
            deployment.getSpec().setReplicas(replicas);
        }
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void scaleStatefulSet(KCluster kCluster, String namespace, String deploymentName, int replicas) {
        StatefulSet statefulSet = getStatefulSet(kCluster, namespace, deploymentName);
        if (statefulSet != null && statefulSet.getSpec() != null) {
            statefulSet.getSpec().setReplicas(replicas);
        }
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public Service getService(KCluster kCluster, String namespace, String serviceName) {
        return services.get(new ResourceKey(clusterKey(kCluster), namespace, serviceName));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public PodList getPods(KCluster kCluster, String namespace) {
        return pods.getOrDefault(new NamespaceKey(clusterKey(kCluster), namespace), EMPTY_POD_LIST);
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public String getLogs(KCluster kCluster, String namespace, String podName, String containerName, int limit) {
        return logs.getOrDefault(new LogKey(clusterKey(kCluster), namespace, podName, containerName), "");
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void createOrReplaceConfigMap(KCluster kCluster, String namespace, String configMapName, List<ConfigFile> configFiles) {
        Map<String, String> data = new HashMap<>();
        configFiles.forEach(configFile -> data.put(configFile.getFileName(), configFile.getFileContent()));
        configMaps.put(new ResourceKey(clusterKey(kCluster), namespace, configMapName), new ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapName)
                .withNamespace(namespace)
                .endMetadata()
                .withData(data)
                .build());
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void deleteConfigMapIfExists(KCluster kCluster, String namespace, String configMapNamePrefix) {
        List<ResourceKey> keysToRemove = new ArrayList<>();
        for (ResourceKey key : configMaps.keySet()) {
            if (!key.cluster().equals(clusterKey(kCluster)) || !key.namespace().equals(namespace)) {
                continue;
            }
            if (hasReleaseName(key.name(), configMapNamePrefix)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(configMaps::remove);
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void createOrReplaceBasicAuth(KCluster kCluster, String namespace, String secretName, String basicAuthCredentials) {
        secrets.put(new ResourceKey(clusterKey(kCluster), namespace, secretName), new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .withNamespace(namespace)
                .endMetadata()
                .withData(Map.of("auth", basicAuthCredentials))
                .build());
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public void deleteSecretIfExists(KCluster kCluster, String namespace, String secretName) {
        secrets.remove(new ResourceKey(clusterKey(kCluster), namespace, secretName));
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public byte[] readClusterConfigBytesFromSecret(String namespace, String secretName) {
        byte[] bytes = clusterConfigSecrets.get(new ResourceKey(ClusterKey.LOCAL.value(), namespace, secretName));
        if (bytes != null) {
            return Arrays.copyOf(bytes, bytes.length);
        }

        Secret secret = secrets.get(new ResourceKey(ClusterKey.LOCAL.value(), namespace, secretName));
        if (secret == null) {
            throw new NoSuchElementException(String.format("Secret %s not found namespace %s", secretName, namespace));
        }

        String encoded = secret.getData().get("value");
        if (encoded == null) {
            throw new NoSuchElementException("Expected field not found in secret");
        }
        return Base64.getDecoder().decode(encoded);
    }

    @Loggable(LogLevel.DEBUG)
    @Override
    public KubernetesClient getDirectClient(KCluster kCluster) {
        throw new UnsupportedOperationException("DummyKubernetesApiClientService does not expose a KubernetesClient");
    }

    private static boolean hasReleaseName(String resourceName, String releaseName) {
        return Objects.equals(resourceName, releaseName) || resourceName.startsWith(releaseName + "-");
    }

    private static String namespaceKey(KCluster kCluster, String namespace) {
        return clusterKey(kCluster) + "::" + namespace;
    }

    private static String clusterKey(KCluster kCluster) {
        if (kCluster == null) {
            return ClusterKey.LOCAL.value();
        }
        if (kCluster.getId() != null) {
            return "cluster-id:" + kCluster.getId();
        }
        return "cluster-ref:" + System.identityHashCode(kCluster);
    }

    private record NamespaceKey(String cluster, String namespace) {
    }

    private record ResourceKey(String cluster, String namespace, String name) {
    }

    private record LogKey(String cluster, String namespace, String podName, String containerName) {
    }

    private enum ClusterKey {
        LOCAL("local");

        private final String value;

        ClusterKey(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
