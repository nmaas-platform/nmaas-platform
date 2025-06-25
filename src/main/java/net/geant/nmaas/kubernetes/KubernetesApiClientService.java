package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import org.springframework.stereotype.Component;

import java.util.Map;
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
            return client.namespaces().withName(namespace).isReady();
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
            return client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();
        }
    }

    public StatefulSet getStatefulSet(KCluster kCluster, String statefulSetName, String namespace) {
        try (KubernetesClient client = initClient(kCluster)) {
            return client.apps()
                    .statefulSets()
                    .inNamespace(namespace)
                    .withName(statefulSetName)
                    .get();
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

    private KubernetesClient initClient(KCluster kCluster) {
        KubernetesClient client;
        if (Objects.nonNull(kCluster)) {
            client = KubernetesApiClientFactory.getClient(kCluster);
        } else {
            client = kubernetesApiClientFactory.getClient();
        }
        return client;
    }

}
