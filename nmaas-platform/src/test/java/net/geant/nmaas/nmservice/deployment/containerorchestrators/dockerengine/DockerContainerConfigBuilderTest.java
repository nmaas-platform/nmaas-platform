package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;
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

    private DockerEngineNmServiceInfo serviceInfo;
    private DockerContainer dockerContainer;
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() throws UnknownHostException {
        DockerContainerTemplate testTemplate1 = new DockerContainerTemplate(TEST_IMAGE_NAME_1);
        testTemplate1.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8080));
        DockerHost testDockerHost1 = new DockerHost(
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
        serviceInfo = new DockerEngineNmServiceInfo(deploymentId, applicationId, clientId, testTemplate1);
        serviceInfo.setHost(testDockerHost1);
        serviceInfo.setManagedDevicesIpAddresses(Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3"));
        DockerNetworkIpam addresses = new DockerNetworkIpam("1.1.0.0/24", "1.1.1.254");
        dockerContainer = new DockerContainer();
        dockerContainer.setNetworkDetails(new DockerContainerNetDetails(1234, addresses));
    }

    @Test(expected = NmServiceRequestVerificationException.class)
    public void shouldVerifySpecAndThrowException() throws NmServiceRequestVerificationException {
        ContainerConfigBuilder.verifyInitInput(serviceInfo);
    }

    @Test
    public void shouldVerifyInitInput() throws NmServiceRequestVerificationException {
        serviceInfo.setDockerContainer(new DockerContainer());
        ContainerConfigBuilder.verifyInitInput(serviceInfo);
    }

    @Test(expected = NmServiceRequestVerificationException.class)
    public void shouldVerifyFinalInputAndThrowException() throws NmServiceRequestVerificationException {
        serviceInfo.setDockerContainer(new DockerContainer());
        ContainerConfigBuilder.verifyFinalInput(serviceInfo);
    }

    @Test
    public void shouldVerifyFinalInputAndContinue() throws NmServiceRequestVerificationException {
        serviceInfo.setDockerContainer(dockerContainer);
        ContainerConfigBuilder.verifyFinalInput(serviceInfo);
    }

    @Test
    public void shouldBuildSimpleConfig() {
        serviceInfo.setDockerContainer(dockerContainer);
        ContainerConfig result = ContainerConfigBuilder.build(serviceInfo);
        assertThat(result.image(), equalTo(TEST_IMAGE_NAME_1));
    }

}