package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.ContainerNetworkDetailsVerificationException;
import net.geant.nmaas.servicedeployment.exceptions.ServiceVerificationException;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkDetails;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkConfigBuildingTest {

    private static final String TEST_IMAGE_NAME_1 = "test-service-1";

    private DockerContainerSpec spec;
    private DockerEngineContainerTemplate testTemplate1;
    private DockerHost testDockerHost1;
    private ContainerNetworkDetails testNetworkDetails1;
    private NmServiceInfo serviceInfo;
    private ContainerNetworkIpamSpec ipamSpec;

    @Before
    public void setup() throws UnknownHostException {
        testTemplate1 = new DockerEngineContainerTemplate("testServiceTemplate1", TEST_IMAGE_NAME_1);
        spec = new DockerContainerSpec("testService1", System.nanoTime(), testTemplate1);
        testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                "/data/volumes", true);
        ipamSpec = new ContainerNetworkIpamSpec("10.10.1.0/24", "10.10.1.0/24", "10.10.1.254");
        testNetworkDetails1 = new ContainerNetworkDetails(ipamSpec, 123);

        serviceInfo = new NmServiceInfo("testService1", NmServiceInfo.ServiceState.INIT, spec);
    }

    @Test(expected = ServiceVerificationException.class)
    public void shouldThrowExceptionOnMissingDeploymentHost() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ServiceVerificationException.class)
    public void shouldThrowExceptionOnMissingDeploymentNetworkDetails() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test
    public void shouldBuildCorrectNetworkConfig() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        serviceInfo.setNetwork(testNetworkDetails1);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ContainerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingIpamSpec() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkDetails testNetworkDetailsWithMissingIpamSpec = new ContainerNetworkDetails(null, 123);
        serviceInfo.setNetwork(testNetworkDetailsWithMissingIpamSpec);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ContainerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingIpamSpecParam() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkIpamSpec incompleteIpamSpec = new ContainerNetworkIpamSpec("", "10.10.1.0/24", "10.10.1.254");
        ContainerNetworkDetails testNetworkDetailsWithIncompleteIpamSpec = new ContainerNetworkDetails(incompleteIpamSpec, 123);
        serviceInfo.setNetwork(testNetworkDetailsWithIncompleteIpamSpec);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

    @Test(expected = ContainerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnInvalidIpamSpecParams() throws ServiceVerificationException, ContainerNetworkDetailsVerificationException {
        serviceInfo.setHost(testDockerHost1);
        ContainerNetworkIpamSpec invalidIpamSpec = new ContainerNetworkIpamSpec("10.10.1.0/16", "10.10.1.0/24", "10.10.1.254");
        ContainerNetworkDetails testNetworkDetailsWithIncompleteIpamSpec = new ContainerNetworkDetails(invalidIpamSpec, 123);
        serviceInfo.setNetwork(testNetworkDetailsWithIncompleteIpamSpec);
        ContainerNetworkConfigBuilder.build(serviceInfo);
    }

}
