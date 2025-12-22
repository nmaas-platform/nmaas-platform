package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.configuration.ConfigFile;
import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.domain.KeyValueView;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesApiJanitorService {

    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesApiClientService kubernetesApiClientService;

    public boolean checkIfNamespaceExists(KCluster kCluster, String namespace) {
        log.info("Checking if namespace {} exists on cluster {}", namespace, kCluster.getId());
        return kubernetesApiClientService.checkIfNamespaceExists(kCluster, namespace);
    }

    public void createNamespace(String namespace, List<KeyValueView> annotations) {
        createNamespace(null, namespace, annotations);
    }

    public void createNamespace(KCluster kCluster, String namespace, List<KeyValueView> annotations) {
        log.info("Creating namespace {} with {} annotations on cluster {}",
                namespace,
                annotations.size(),
                Objects.nonNull(kCluster) ? kCluster.getId() : "LOCAL");
        Map<String, String> annotationsMap = new HashMap<>();
        annotations.forEach(a -> annotationsMap.put(a.getKey(), a.getValue()));
        kubernetesApiClientService.createNamespace(kCluster, namespace, annotationsMap);
    }

    public boolean checkIfReady(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        log.info("Checking status of release {} in namespace {}", deploymentId.value(), namespace);
        final List<Deployment> deployments = kubernetesApiClientService.getDeployments(kCluster, namespace, deploymentId.value());
        for (Deployment d : deployments) {
            if (d.getSpec().getReplicas() > Optional.ofNullable(d.getStatus().getReadyReplicas()).orElse(0)) {
                return false;
            }
        }
        final List<StatefulSet> statefulSets = kubernetesApiClientService.getStatefulSets(kCluster, namespace, deploymentId.value());
        for (StatefulSet s : statefulSets) {
            if (s.getSpec().getReplicas() > Optional.ofNullable(s.getStatus().getReadyReplicas()).orElse(0)) {
                return false;
            }
        }
        return true;
    }

    public String retrieveServiceIp(KCluster kCluster, Identifier serviceName, String domain) {
        final String namespace = namespaceService.namespace(domain);
        final Service service = kubernetesApiClientService.getService(kCluster, namespace, serviceName.value());
        if (Objects.nonNull(service)) {
            try {
                return service.getStatus().getLoadBalancer().getIngress().getFirst().getIp();
            } catch (Exception e) {
                log.warn("Service {} found but encountered problem with retrieving IP address: {}", serviceName.value(), e.getMessage());
                throw new JanitorException("Not able to retrieve IP information: " + e.getMessage());
            }
        } else {
            log.info("Service {} not found in namespace {}.", serviceName.value(), namespace);
            throw new JanitorException(
                    String.format("Not able to retrieve IP information. No service with name %s found in namespace %s", serviceName.value(), namespace)
            );
        }
    }

    public boolean checkServiceExists(KCluster kCluster, Identifier serviceName, String domain) {
        final String namespace = namespaceService.namespace(domain);
        final Service service = kubernetesApiClientService.getService(kCluster, namespace, serviceName.value());
        return Objects.nonNull(service);
    }

    public List<AppComponentDetails> getPodNames(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        final PodList pods = kubernetesApiClientService.getPods(kCluster, namespace);
        return pods.getItems().stream()
                .filter(p -> p.getMetadata().getName().startsWith(deploymentId.value()))
                .map(p ->
                        new AppComponentDetails(
                                p.getMetadata().getName(),
                                p.getMetadata().getName(),
                                p.getSpec().getContainers().stream().map(Container::getName).toList())
                )
                .toList();
    }

    public List<String> getPodLogs(KCluster kCluster, String podName, String containerName, String domain, int limit) {
        final String namespace = namespaceService.namespace(domain);
        return Collections.singletonList(
                kubernetesApiClientService.getLogs(kCluster, namespace, podName, containerName, limit)
        );
    }

    public void createOrReplaceConfigMaps(KCluster kCluster, Identifier deploymentId, String domain, List<ConfigFile> configFiles) {
        final String namespace = namespaceService.namespace(domain);
        Map<String, List<ConfigFile>> configFilesInConfigMaps = new HashMap<>();
        configFiles.forEach(configFile -> {
            final String configMapName = generateConfigMapName(deploymentId, configFile.getFilePath(), configFile.getFileName());
            if (configFilesInConfigMaps.containsKey(configMapName)) {
                List<ConfigFile> currentList = new ArrayList<>(configFilesInConfigMaps.get(configMapName));
                currentList.add(configFile);
                configFilesInConfigMaps.replace(configMapName, currentList);
            } else {
                configFilesInConfigMaps.put(configMapName, List.of(configFile));
            }
        });
        configFilesInConfigMaps.keySet().forEach(configMapName ->
                kubernetesApiClientService.createOrReplaceConfigMap(kCluster, namespace, configMapName, configFilesInConfigMaps.get(configMapName))
        );
    }

    private static String generateConfigMapName(Identifier deploymentId, String filePath, String fileName) {
        final String fileDirectory = filePath.replace(fileName, "").replace("/", "");
        return fileDirectory.isBlank() ? deploymentId.value() : deploymentId.value() + "-" + fileDirectory;
    }

    public void deleteConfigMapIfExists(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        kubernetesApiClientService.deleteConfigMapIfExists(kCluster, namespace, deploymentId.value());
    }

    public void createOrReplaceBasicAuth(KCluster kCluster, Identifier deploymentId, String domain, String basicAuthUsername, String basicAuthPassword) {
        final String namespace = namespaceService.namespace(domain);
        kubernetesApiClientService.createOrReplaceBasicAuth(kCluster, namespace, generateSecretName(deploymentId), generateBasicAuthCredentials(basicAuthUsername, basicAuthPassword));
    }

    private static String generateSecretName(Identifier deploymentId) {
        return deploymentId.value() + "-auth";
    }

    private static String generateBasicAuthCredentials(String basicAuthUsername, String basicAuthPassword) {
        final String valueToEncode = basicAuthUsername + ":" + basicAuthPassword;
        return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public void deleteBasicAuthIfExists(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        kubernetesApiClientService.deleteSecretIfExists(kCluster, namespace, generateSecretName(deploymentId));
    }

    public void deleteTlsIfExists(KCluster kCluster, Identifier deploymentId, String domain) {
        final String namespace = namespaceService.namespace(domain);
        kubernetesApiClientService.deleteSecretIfExists(kCluster, namespace, generateTlsSecretName(deploymentId));
    }

    private static String generateTlsSecretName(Identifier deploymentId) {
        return deploymentId.value() + "-tls";
    }

}