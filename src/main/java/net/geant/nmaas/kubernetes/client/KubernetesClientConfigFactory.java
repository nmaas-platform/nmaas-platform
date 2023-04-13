package net.geant.nmaas.kubernetes.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Provides KubernetesClient instance with suitable configuration depending on the Platform deployment
 * (to avoid multiple creation of client entity)
 */
@Component
@Log4j2
public class KubernetesClientConfigFactory {

    @Value("${nmaas.kubernetes.incluster:true}")
    private boolean inCluster;

    @Value("${nmaas.kubernetes.apiserver.url:none}")
    private String master;

    // currently only used for testing purposes
    private static final String OAUTH_TOKEN = "TODO REPLACE";

    private Config config;
    private KubernetesClient client;

    /**
     * Client is created only when necessary
     * @return KubernetesClient instance
     */
    public synchronized KubernetesClient getClient() {
        if (this.client == null) {
            this.client = new DefaultKubernetesClient(getConfig());
        }
        return this.client;
    }

    /**
     * Lazy configuration creation
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
            log.info(String.format("Kubernetes API server master url: %s", master));
            return new ConfigBuilder().withMasterUrl(master)
                    .withTrustCerts(true)
                    .withOauthToken(OAUTH_TOKEN)
                    .build();
        }
    }

    @PreDestroy
    protected void preDestroy() {
        client.close();
    }

}
