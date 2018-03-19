package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerHostNetworkResourceManagerTest {

    @Autowired
    private DockerNetworkResourceManager networkManager;
    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerHostNetworkRepositoryManager dockerHostNetworkRepositoryManager;

    private static final String DOMAIN = "domain1";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");

    @Before
    public void setup() throws Exception {
        DockerHost testDockerHost1 = dockerHost();
        dockerHostRepositoryManager.addDockerHost(testDockerHost1);
        DockerHostNetwork testDockerHostNetwork1 = new DockerHostNetwork(DOMAIN, testDockerHost1, 123, "10.10.1.0/24", "10.10.1.254");
        dockerHostNetworkRepositoryManager.storeNetwork(testDockerHostNetwork1);
    }

    @Test
    public void shouldReturnNetworkDetails() throws ContainerOrchestratorInternalErrorException {
        assertThat(networkManager.obtainGatewayFromClientNetwork(DOMAIN), equalTo("10.10.1.254"));
        assertThat(networkManager.obtainSubnetFromClientNetwork(DOMAIN), equalTo("10.10.1.0/24"));
        assertThat(networkManager.obtainPortForClientNetwork(DOMAIN, deploymentId1), equalTo(1000));
        assertThat(networkManager.obtainPortForClientNetwork(DOMAIN, deploymentId2), equalTo(1001));
        assertThat(networkManager.assignNewIpAddressForContainer(DOMAIN), equalTo("10.10.1.1"));
        assertThat(networkManager.assignNewIpAddressForContainer(DOMAIN), equalTo("10.10.1.2"));
        networkManager.removeAddressAssignment(DOMAIN,"10.10.1.1");
        assertThat(networkManager.assignNewIpAddressForContainer(DOMAIN), equalTo("10.10.1.1"));
        assertThat(networkManager.assignNewIpAddressForContainer(DOMAIN), equalTo("10.10.1.3"));
    }

    private DockerHost dockerHost() throws UnknownHostException {
        return new DockerHost(
                    "testHost1",
                    InetAddress.getByName("1.1.1.1"),
                    1234,
                    InetAddress.getByName("1.1.1.1"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.10.0.0"),
                    "/data/scripts",
                    "/data/volumes",
                    true);
    }

}
