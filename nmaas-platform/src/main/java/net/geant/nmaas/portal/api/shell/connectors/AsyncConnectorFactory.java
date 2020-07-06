package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import org.springframework.stereotype.Component;

/**
 * This object is responsible for creating connectors to instances
 * currently utilizes Kubernetes Connector (still to be provided)
 */
@Component
@AllArgsConstructor
public class AsyncConnectorFactory {

    private static final String OAUTH_TOKEN = "TODO REPLACE";

    public AsyncConnector prepareConnection(AppInstance appInstance) {

        // TODO retrieve data from app instance
        final String master = "https://master.url";
        final String namespace = "namespace";
        final String podName = "pod-name";

        Config config = new ConfigBuilder().withMasterUrl(master)
                .withTrustCerts(true)
                .withOauthToken(OAUTH_TOKEN)
                .build();

        return new KubernetesConnector(config, namespace, podName);

    }
}
