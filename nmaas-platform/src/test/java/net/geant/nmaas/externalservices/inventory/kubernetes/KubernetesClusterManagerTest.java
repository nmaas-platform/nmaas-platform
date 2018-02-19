package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class KubernetesClusterManagerTest {

    private static final String REST_API_HOST_ADDRESS = "10.10.1.1";
    private static final int REST_API_PORT = 9000;
    private static final String HELM_HOST_CHARTS_DIRECTORY = "/home/charts";
    private static final String HELM_HOST_ADDRESS = "10.10.1.2";
    private static final String HELM_HOST_SSH_USERNAME = "test";

    @Autowired
    private KubernetesClusterRepository repository;
    @Autowired
    private ModelMapper modelMapper;

    private KubernetesClusterManager manager;

    @Before
    public void setup() {
        manager = new KubernetesClusterManager(repository, modelMapper);
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void shouldRetrieveClusterDetails() throws UnknownHostException {
        repository.save(simpleKubernetesCluster("cluster1"));
        assertThat(manager.getHelmHostChartsDirectory(), equalTo(HELM_HOST_CHARTS_DIRECTORY));
        assertThat(manager.getHelmHostAddress(), equalTo(HELM_HOST_ADDRESS));
        assertThat(manager.getHelmHostSshUsername(), equalTo(HELM_HOST_SSH_USERNAME));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingCluster() {
        manager.getHelmHostAddress();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnTooManyClusters() throws UnknownHostException {
        repository.save(simpleKubernetesCluster("cluster1"));
        repository.save(simpleKubernetesCluster("cluster2"));
        manager.getHelmHostAddress();
    }

    @Test
    public void shouldReserveExternalNetworks() throws UnknownHostException, ExternalNetworkNotFoundException {
        repository.save(simpleKubernetesCluster("cluster1"));
        Identifier client10 = Identifier.newInstance("10");
        ExternalNetworkView network10 = manager.reserveExternalNetwork(client10);
        Identifier client20 = Identifier.newInstance("20");
        ExternalNetworkView network20 = manager.reserveExternalNetwork(client20);
        assertThat(network10.getExternalIp().getHostAddress(), not(equalTo(network20.getExternalIp().getHostAddress())));
    }

    @Test(expected = ExternalNetworkNotFoundException.class)
    public void shouldFailToReserveExternalNetworks() throws UnknownHostException, ExternalNetworkNotFoundException {
        repository.save(simpleKubernetesCluster("cluster1"));
        manager.reserveExternalNetwork(Identifier.newInstance("10"));
        manager.reserveExternalNetwork(Identifier.newInstance("20"));
        manager.reserveExternalNetwork(Identifier.newInstance("30"));
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
