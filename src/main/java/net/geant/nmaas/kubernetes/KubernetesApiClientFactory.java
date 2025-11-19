package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides KubernetesClient instance with suitable configuration depending on the Platform deployment
 * (to avoid multiple creation of client entity)
 */
@Component
@Slf4j
class KubernetesApiClientFactory {

    @Value("${nmaas.kubernetes.incluster:true}")
    private boolean inCluster;

    @Value("${nmaas.kubernetes.apiserver.url:none}")
    private String master;

    // currently only used for testing purposes
    private static final String OAUTH_TOKEN = "TODO REPLACE";

    private Config config;

    /**
     *
     * @return KubernetesClient instance
     */
    public synchronized KubernetesClient getClient() {
        return new KubernetesClientBuilder().withConfig(getConfig()).build();
    }

    /**
     * Client for remote cluster instantiated each time when requested
     *
     * @return KubernetesClient instance
     */
    public static KubernetesClient getClient(KCluster cluster) {
        try {
            final Config config = Config.fromKubeconfig(Files.readString(Path.of(cluster.getPathConfigFile())));
            return new KubernetesClientBuilder().withConfig(config).build();
        } catch (IOException e) {
            throw new KubernetesClientSetupException(e);
        }
    }

    /**
     * Lazy configuration creation
     *
     * @return KubernetesClient configuration
     */
    private Config getConfig() {
        if (this.config == null) {
            this.config = makeConfig();
        }
        return this.config;
    }

    private Config makeConfig() {
        if (inCluster) {
            log.info("Using in cluster Kubernetes client configuration");
            return new ConfigBuilder().build();
        } else {
            log.info("Kubernetes API server master url: {}", master);
            return new ConfigBuilder().withMasterUrl(master)
                    .withTrustCerts(true)
                    .withOauthToken(OAUTH_TOKEN)
                    .build();
        }
    }

}
