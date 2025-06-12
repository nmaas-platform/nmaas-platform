package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesApiService {

    private final KubernetesApiClientFactory kubernetesApiClientFactory;

    public String getKubernetesVersion(KCluster kCluster) {
        KubernetesClient client = initClient(kCluster);
        final String version = String.join("-", client.getKubernetesVersion().getMajor(), client.getKubernetesVersion().getMinor());
        client.close();
        return version;
    }

    public void scaleDeployment(KCluster kCluster, String namespace, String deploymentName, int replicas) {
        KubernetesClient client = initClient(kCluster);
        client.apps()
                .deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .scale(replicas);
        client.close();
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
