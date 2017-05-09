package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkConfigBuilder;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkDetailsVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerNetworkConfigBuildingTest {

    private Identifier clientId = Identifier.newInstance("testClientId");
    private DockerHost testDockerHost1;
    private DockerNetwork testDockerNetwork1;

    @Before
    public void setup() throws UnknownHostException {
        testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.10.0.0"),
                "/data/volumes", true);
        testDockerNetwork1 = new DockerNetwork(clientId, testDockerHost1, 123, "10.10.1.0/24", "10.10.1.254");
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingDockerHost() throws DockerNetworkDetailsVerificationException {
        testDockerNetwork1.setDockerHost(null);
        DockerNetworkConfigBuilder.build(testDockerNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingVLAN() throws DockerNetworkDetailsVerificationException {
        testDockerNetwork1.setVlanNumber(0);
        DockerNetworkConfigBuilder.build(testDockerNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingSubnet() throws DockerNetworkDetailsVerificationException {
        testDockerNetwork1.setSubnet(null);
        DockerNetworkConfigBuilder.build(testDockerNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingGateway() throws DockerNetworkDetailsVerificationException {
        testDockerNetwork1.setGateway(null);
        DockerNetworkConfigBuilder.build(testDockerNetwork1);
    }

    @Test
    public void shouldBuildCorrectNetworkConfig() throws DockerNetworkDetailsVerificationException {
        DockerNetworkConfigBuilder.build(testDockerNetwork1);
    }

}
