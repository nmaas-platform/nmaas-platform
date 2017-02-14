package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigInput;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigBuilderTest {

    private static final String TEST_IMAGE_NAME_1 = "test-service-1";
    private static final String TEST_SERVICE_NAME_1 = "testService1";
    private static final String TEST_SERVICE_TEMPLATE_NAME_1 = "testServiceTemplate1";
    private static final Identifier TEST_APPLICATION_ID_1 = Identifier.newInstance(TEST_SERVICE_TEMPLATE_NAME_1);

    private DockerContainerSpec spec;
    private DockerEngineContainerTemplate testTemplate1;
    private DockerHost testDockerHost1;
    private NmServiceInfo serviceInfo;

    @Before
    public void setup() throws UnknownHostException {
        testTemplate1 = new DockerEngineContainerTemplate(TEST_APPLICATION_ID_1, TEST_SERVICE_TEMPLATE_NAME_1, TEST_IMAGE_NAME_1);
        spec = new DockerContainerSpec(TEST_SERVICE_NAME_1, testTemplate1);
        testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.10.0.0"),
                "/data/volumes", true);
        serviceInfo = new NmServiceInfo(TEST_SERVICE_NAME_1, NmServiceDeploymentState.INIT, spec);
    }

    @Test(expected = NmServiceVerificationException.class)
    public void shouldVerifySpecAndThrowException() throws NmServiceVerificationException {
        ContainerConfigBuilder.verifyInput(serviceInfo);
    }

    @Test
    public void shouldVerifySpecAndContinue() throws NmServiceVerificationException {
        spec.setClientDetails("testClient1", "testOrganisation1");
        ContainerConfigBuilder.verifyInput(serviceInfo);
    }

    @Test
    public void shouldBuildSimpleConfig() {
        ContainerConfig result = ContainerConfigBuilder.build(ContainerConfigInput.fromSpec(spec), testDockerHost1, Arrays.asList(1000, 1001));
        assertEquals(TEST_IMAGE_NAME_1, result.image());
    }

}
