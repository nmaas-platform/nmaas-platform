package net.geant.nmaas.nmservice.deployment;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.util.Config;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesApiConnector;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class KubernetesApiConnectorTest {

    @Value("${kubernetes.api.url}")
    private String kubernetesApiUrl;

    @Autowired
    private KubernetesApiConnector connector;

    @Ignore
    @Test
    public void shouldCheckCluster() throws KubernetesClusterCheckException {
        connector.checkClusterStatusAndPrerequisites();
    }

    @Ignore
    @Test
    public void basicTest() throws ApiException {
        ApiClient client = Config.fromUrl(kubernetesApiUrl, false);
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreV1Api = new CoreV1Api();
        assertThat(coreV1Api.listNode(null, null, null, null, 3, false)
                .getItems().size(), Matchers.greaterThan(0));
        ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
        assertThat(extensionsV1beta1Api.listDeploymentForAllNamespaces(null, null, null, null, 5, false)
                .getItems().size(), Matchers.greaterThan(0));
    }

}
