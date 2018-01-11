package net.geant.nmaas.nmservice.deployment;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.util.Config;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesApiConnector;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Set of integration tests verifying correct communication with real Kubernetes REST API.
 * Note: All tests must be ignored.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class KubernetesApiConnectorTest {

    private static final String REST_API_HOST_ADDRESS = "10.134.241.6";
    private static final int REST_API_PORT = 8080;
    private static final String HELM_HOST_CHARTS_DIRECTORY = "/home/charts";
    private static final String HELM_HOST_ADDRESS = "10.10.1.2";
    private static final String HELM_HOST_SSH_USERNAME = "test";

    @Autowired
    private KubernetesApiConnector connector;
    @Autowired
    private KubernetesClusterRepository repository;
    @Autowired
    private KubernetesClusterManager manager;

    @Before
    public void setup() throws UnknownHostException {
        repository.save(simpleKubernetesCluster("CLUSTER1"));
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Ignore
    @Test
    public void shouldCheckCluster() throws KubernetesClusterCheckException {
        connector.checkClusterStatusAndPrerequisites();
    }

    @Ignore
    @Test
    public void basicTest() throws ApiException {
        ApiClient client = Config.fromUrl(manager.getKubernetesApiUrl(), false);
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreV1Api = new CoreV1Api();
        assertThat(coreV1Api.listNode(null, null, null, null, 3, false)
                .getItems().size(), greaterThan(0));
        ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
        assertThat(extensionsV1beta1Api.listDeploymentForAllNamespaces(null, null, null, null, 5, false)
                .getItems().size(), greaterThan(0));
        assertThat(extensionsV1beta1Api.listNamespacedIngress("default", null, null, null, null, 3, false)
                .getItems().size(), greaterThan(0));
    }

    private KubernetesCluster simpleKubernetesCluster(String clusterName) throws UnknownHostException {
        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setName(clusterName);
        cluster.setRestApiHostAddress(InetAddress.getByName(REST_API_HOST_ADDRESS));
        cluster.setRestApiPort(REST_API_PORT);
        cluster.setHelmHostChartsDirectory(HELM_HOST_CHARTS_DIRECTORY);
        cluster.setHelmHostAddress(InetAddress.getByName(HELM_HOST_ADDRESS));
        cluster.setHelmHostSshUsername(HELM_HOST_SSH_USERNAME);
        KubernetesClusterAttachPoint attachPoint = new KubernetesClusterAttachPoint();
        attachPoint.setRouterName("R1");
        attachPoint.setRouterId("172.0.0.1");
        attachPoint.setRouterInterfaceName("ge-0/0/1");
        cluster.setAttachPoint(attachPoint);
        ExternalNetworkSpec externalNetworkSpec1 = new ExternalNetworkSpec();
        externalNetworkSpec1.setExternalIp(InetAddress.getByName("192.168.1.1"));
        externalNetworkSpec1.setExternalNetwork(InetAddress.getByName("192.168.1.0"));
        externalNetworkSpec1.setExternalNetworkMaskLength(24);
        ExternalNetworkSpec externalNetworkSpec2 = new ExternalNetworkSpec();
        externalNetworkSpec2.setExternalIp(InetAddress.getByName("192.168.2.1"));
        externalNetworkSpec2.setExternalNetwork(InetAddress.getByName("192.168.2.0"));
        externalNetworkSpec2.setExternalNetworkMaskLength(24);
        cluster.setExternalNetworks(Arrays.asList(externalNetworkSpec1, externalNetworkSpec2));
        return cluster;
    }

}
