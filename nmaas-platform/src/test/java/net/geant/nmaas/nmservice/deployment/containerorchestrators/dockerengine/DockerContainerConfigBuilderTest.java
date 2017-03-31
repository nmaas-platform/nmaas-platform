package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerConfigBuilderTest {

    private static final String TEST_IMAGE_NAME_1 = "test-service-1";
    private static final String TEST_SERVICE_NAME_1 = "testService1";
    private static final Long TEST_CLIENT_ID = 100L;

    private DockerContainerSpec spec;
    private DockerContainerTemplate testTemplate1;
    private DockerHost testDockerHost1;
    private NmServiceInfo serviceInfo;
    private ContainerNetworkDetails networkDetails;

    @Before
    public void setup() throws UnknownHostException {
        testTemplate1 = new DockerContainerTemplate(TEST_IMAGE_NAME_1);
        testTemplate1.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8080));
        spec = new DockerContainerSpec(TEST_SERVICE_NAME_1, testTemplate1, TEST_CLIENT_ID);
        testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.10.0.0"),
                "/data/volumes",
                true);
        serviceInfo = new NmServiceInfo(TEST_SERVICE_NAME_1, NmServiceDeploymentState.INIT, spec);
        serviceInfo.setHost(testDockerHost1);
        serviceInfo.setManagedDevicesIpAddresses(Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3"));
        ContainerNetworkIpamSpec addresses = new ContainerNetworkIpamSpec("1.1.0.0/24", "1.1.1.254");
        networkDetails = new ContainerNetworkDetails(1234, addresses, 123);
    }

    @Test(expected = NmServiceRequestVerificationException.class)
    public void shouldVerifySpecAndThrowException() throws NmServiceRequestVerificationException {
        ContainerConfigBuilder.verifyInput(serviceInfo);
    }

    @Test
    public void shouldVerifySpecAndContinue() throws NmServiceRequestVerificationException {
        serviceInfo.setNetwork(networkDetails);
        ContainerConfigBuilder.verifyInput(serviceInfo);
    }

    @Test
    public void shouldBuildSimpleConfig() {
        serviceInfo.setNetwork(networkDetails);
        ContainerConfig result = ContainerConfigBuilder.build(serviceInfo);
        assertThat(result.image(), equalTo(TEST_IMAGE_NAME_1));
    }

}
