package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource("classpath:application.properties")
public class KubernetesClientConfigFactory {

    @Value("${nmass.kubernetes.apiserver.url}")
    private String nmaasKubernetesApiserverUrl;

    private static final String OAUTH_TOKEN = "TODO REPLACE";

    private final Config config;
    private KubernetesClient client;

    public KubernetesClientConfigFactory() {
        // TODO fix reading master url from application.properties
        if(nmaasKubernetesApiserverUrl == null) {
            nmaasKubernetesApiserverUrl = "";
        }

        log.info("Kubernetes API server master url:\t" + nmaasKubernetesApiserverUrl);
        config = makeConfig();

    }

    protected Config makeConfig() {
        return new ConfigBuilder().withMasterUrl(nmaasKubernetesApiserverUrl)
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
            client = new DefaultKubernetesClient(config);
        }
        return client;
    }

    public Config getConfig() {
        return config;
    }

    @PreDestroy
    protected void preDestroy() {
        client.close();
    }
}
