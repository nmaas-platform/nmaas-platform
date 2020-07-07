package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * raw testing of kubernetes client
 * probably to be removed later
 */
@Log4j2
public class KubernetesConnectorIntTest {

    private static final String OAUTH_TOKEN = "TODO REPLACE";

    @Test
    @Disabled
    public void retrievePods() {

        String master = "https://10.1.1.11:6443";
        log.info(master);

        final String namespace = "c2";

        Config config = new ConfigBuilder().withMasterUrl(master)
                .withTrustCerts(true)
                .withOauthToken(OAUTH_TOKEN)
                .build();

        KubernetesClient client = new DefaultKubernetesClient(config);

        PodList podList = client.pods().inNamespace(namespace).list();
        podList.getItems().forEach(pod -> log.info(pod.getMetadata().getName()));

        client.close();
    }
}
