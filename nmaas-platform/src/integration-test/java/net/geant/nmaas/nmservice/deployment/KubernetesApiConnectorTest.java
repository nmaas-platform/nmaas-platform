package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesApiConnector;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.InternalErrorException;
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
    public void createOrUpdateIngressObject() throws InternalErrorException {
        connector.createOrUpdateIngressObject(
                "ingress-test-name",
                "service.nmaas.geant.org",
                "service-name",
                80);
    }

    @Ignore
    @Test
    public void deleteIngressRule() throws InternalErrorException {
        connector.deleteIngressRule("ingress-test-name", "service.nmaas.geant.org");
    }

    @Ignore
    @Test
    public void deleteIngressObject() throws InternalErrorException {
        connector.deleteIngressObject("ingress-test-name");
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
