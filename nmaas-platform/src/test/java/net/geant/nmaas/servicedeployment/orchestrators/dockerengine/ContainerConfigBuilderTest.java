package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.ServiceSpecVerificationException;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.ContainerConfigBuilder;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigBuilderTest {

    private static final String TEST_IMAGE_NAME_1 = "test-service-1";

    private DockerContainerSpec spec;
    private DockerEngineContainerTemplate testTemplate1;
    private DockerHost testDockerHost1;

    @Before
    public void setup() throws UnknownHostException {
        testTemplate1 = new DockerEngineContainerTemplate("testServiceTemplate1", TEST_IMAGE_NAME_1);
        spec = new DockerContainerSpec("testService1", System.nanoTime(), testTemplate1);
        testDockerHost1 = new DockerHost("testHost1", InetAddress.getByName("1.1.1.1"), 1234, InetAddress.getByName("1.1.1.1"), "/data/volumes", true);
    }

    @Test(expected = ServiceSpecVerificationException.class)
    public void shouldVerifySpecAndThrowException() throws ServiceSpecVerificationException {
        ContainerConfigBuilder.verifyInput(spec);
    }

    @Test
    public void shouldVerifySpecAndContinue() throws ServiceSpecVerificationException {
        spec.setClientDetails("testClient1", "testOrganisation1");
        ContainerConfigBuilder.verifyInput(spec);
    }

    @Test
    public void shouldBuildSimpleConfig() {
        ContainerConfig result = ContainerConfigBuilder.build(spec, testDockerHost1);
        assertEquals(TEST_IMAGE_NAME_1, result.image());
    }

}
