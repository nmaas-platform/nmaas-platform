package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerNetworkDetailsVerificationException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerNetworkConfigBuildingTest {

    private static final String TEST_IMAGE_NAME_1 = "test-service-1";
    private static final String TEST_SERVICE_NAME_1 = "testService1";
    private static final String TEST_SERVICE_TEMPLATE_NAME_1 = "testServiceTemplate1";
    private static final Identifier TEST_APPLICATION_ID_1 = Identifier.newInstance(TEST_SERVICE_TEMPLATE_NAME_1);
    private static final Long TEST_CLIENT_ID = 100L;

    private DockerContainerSpec spec;
    private DockerContainerTemplate testTemplate1;
    private DockerHost testDockerHost1;
    private ContainerNetworkDetails testNetworkDetails1;
    private NmServiceInfo serviceInfo;
    private ContainerNetworkIpamSpec ipamSpec;

    @Before
    public void setup() throws UnknownHostException {
        testTemplate1 = new DockerContainerTemplate(TEST_IMAGE_NAME_1);
        spec = new DockerContainerSpec(TEST_SERVICE_NAME_1, testTemplate1, TEST_CLIENT_ID);
        testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.10.0.0"),
                "/data/volumes", true);
        ipamSpec = new ContainerNetworkIpamSpec("10.10.1.0/24", "10.10.1.254");
        testNetworkDetails1 = new ContainerNetworkDetails(1234, ipamSpec, 123);
        serviceInfo = new NmServiceInfo(TEST_SERVICE_NAME_1, NmServiceDeploymentState.INIT, spec);
        serviceInfo.setAppDeploymentId(TEST_APPLICATION_ID_1.value());
    }

    @Test(expected = NmServiceRequestVerificationException.class)
    public void shouldThrowExceptionOnMissingDeploymentHost() throws NmServiceRequestVerificationException, ContainerNetworkDetailsVerificationException {
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = NmServiceRequestVerificationException.class)
    public void shouldThrowExceptionOnMissingDeploymentNetworkDetails() throws NmServiceRequestVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test
    public void shouldBuildCorrectNetworkConfig() throws NmServiceRequestVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        serviceInfo.setNetwork(testNetworkDetails1);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ContainerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingIpamSpec() throws NmServiceRequestVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkDetails testNetworkDetailsWithMissingIpamSpec = new ContainerNetworkDetails(1234, null, 123);
        serviceInfo.setNetwork(testNetworkDetailsWithMissingIpamSpec);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ContainerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingIpamSpecParam() throws NmServiceRequestVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkIpamSpec incompleteIpamSpec = new ContainerNetworkIpamSpec("10.10.1.0/24", "");
        ContainerNetworkDetails testNetworkDetailsWithIncompleteIpamSpec = new ContainerNetworkDetails(1234, incompleteIpamSpec, 123);
        serviceInfo.setNetwork(testNetworkDetailsWithIncompleteIpamSpec);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

}
