package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import javax.annotation.PreDestroy;

/**
 * provides standard nmass-shell service account connection configuration and KubernetesClient instance
 * (to avoid multiple creation of client entity)
 */
@Component
@Log4j2
public class KubernetesClientConfigFactory {

    @Value("${nmass.kubernetes.apiserver.url:none}")
    private String master;

    private static final String OAUTH_TOKEN = "TODO REPLACE";

    private Config config;
    private KubernetesClient client;

    protected Config makeConfig() {
        log.info("Kubernetes API server master url:\t" + master);
        return new ConfigBuilder().withMasterUrl(master)
                .withTrustCerts(true)
                .withOauthToken(OAUTH_TOKEN)
                .build();
    }

    /**
     * Client is created only when necessary
     * @return KubernetesClient instance
     */
    public synchronized KubernetesClient getClient() {
        if(this.client == null) {
            this.client = new DefaultKubernetesClient(getConfig());
        }
        return this.client;
    }

    /**
     * Lazy configuration creation
     * @return KubernetesClient configuration
     */
    public synchronized Config getConfig() {
        if(this.config == null) {
            this.config = makeConfig();
        }
        return this.config;
    }

    @PreDestroy
    protected void preDestroy() {
        client.close();
    }
}
