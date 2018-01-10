package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KubernetesClusterManagerTest {

    private static final String REST_API_HOST_ADDRESS = "10.10.1.1";
    private static final int REST_API_PORT = 9000;
    private static final String HELM_HOST_CHARTS_DIRECTORY = "/home/charts";
    private static final String HELM_HOST_ADDRESS = "10.10.1.2";
    private static final String HELM_HOST_SSH_USERNAME = "test";

    private KubernetesClusterManager manager;
    private KubernetesClusterRepository repository = mock(KubernetesClusterRepository.class);

    @Before
    public void setup() {
        manager = new KubernetesClusterManager(repository);
    }

    @Test
    public void shouldRetrieveClusterDetails() throws UnknownHostException {
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Arrays.asList(simpleKubernetesCluster("cluster1")));
        manager = new KubernetesClusterManager(repository);
        assertThat(manager.getKubernetesApiUrl(), equalTo("http://" + REST_API_HOST_ADDRESS + ":" + REST_API_PORT));
        assertThat(manager.getHelmHostChartsDirectory(), equalTo(HELM_HOST_CHARTS_DIRECTORY));
        assertThat(manager.getHelmHostAddress(), equalTo(HELM_HOST_ADDRESS));
        assertThat(manager.getHelmHostSshUsername(), equalTo(HELM_HOST_SSH_USERNAME));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingCluster() {
        when(repository.count()).thenReturn(0L);
        manager.getHelmHostAddress();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnTooManyClusters() throws UnknownHostException {
        when(repository.count()).thenReturn(2L);
        manager.getHelmHostAddress();
    }

    private KubernetesCluster simpleKubernetesCluster(String clusterName) throws UnknownHostException {
        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setName(clusterName);
        cluster.setRestApiHostAddress(InetAddress.getByName(REST_API_HOST_ADDRESS));
        cluster.setRestApiPort(REST_API_PORT);
        cluster.setHelmHostChartsDirectory(HELM_HOST_CHARTS_DIRECTORY);
        cluster.setHelmHostAddress(InetAddress.getByName(HELM_HOST_ADDRESS));
        cluster.setHelmHostSshUsername(HELM_HOST_SSH_USERNAME);
        return cluster;
    }

}
