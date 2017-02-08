package net.geant.nmaas.servicedeployment;

import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.nmservicedeployment.exceptions.*;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.DockerNetworkClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineContainerNetworkIntTest {

    @Autowired
    private DockerNetworkClient networkClient;

    @Autowired
    private DockerHostRepository dockerHostRepository;

    private ContainerNetworkDetails testNetworkDetails1;
    private NmServiceInfo serviceInfo;
    private ContainerNetworkIpamSpec ipamSpec;

    @Before
    public void setup() throws UnknownHostException, DockerHostNotFoundException {
        DockerContainerSpec spec = new DockerContainerSpec("testService1", System.nanoTime(), null);
        spec.setClientDetails("client1", "company1");
        ipamSpec = new ContainerNetworkIpamSpec("10.10.0.0/16", "10.10.1.0/24", "10.10.1.254");
        testNetworkDetails1 = new ContainerNetworkDetails(ipamSpec, 123);
        serviceInfo = new NmServiceInfo("testService1", NmServiceInfo.ServiceState.INIT, spec);
        serviceInfo.setHost(dockerHostRepository.loadPreferredDockerHost());
        serviceInfo.setNetwork(testNetworkDetails1);
    }

    @Test
    public void shouldCreateInspectAndRemoteSimpleNetwork()
            throws NmServiceVerificationException, ContainerNetworkDetailsVerificationException, ContainerOrchestratorInternalErrorException, CouldNotCreateContainerNetworkException, ContainerNetworkCheckFailedException, CouldNotCheckNmServiceStateException, InterruptedException, CouldNotRemoveContainerNetworkException {
        final NetworkConfig networkConfig = ContainerNetworkConfigBuilder.build(serviceInfo);
        final DockerHost host = (DockerHost) serviceInfo.getHost();
        final String networkId = networkClient.create(networkConfig, host);
        assertThat(networkId, is(notNullValue()));
        networkClient.checkNetwork(networkId, networkConfig, host);
        Thread.sleep(5000);
        networkClient.remove(networkId, host);
    }

}
