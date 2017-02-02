package net.geant.nmaas.servicedeployment;

import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostsRepository;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.DockerNetworkClient;
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
    private DockerHostsRepository dockerHostsRepository;

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
        serviceInfo.setHost(dockerHostsRepository.loadPreferredDockerHost());
        serviceInfo.setNetwork(testNetworkDetails1);
    }

    @Test
    public void shouldCreateInspectAndRemoteSimpleNetwork()
            throws ServiceVerificationException, ContainerNetworkDetailsVerificationException, OrchestratorInternalErrorException, CouldNotCreateContainerNetworkException, ContainerNetworkCheckFailedException, CouldNotCheckNmServiceStateException, InterruptedException, CouldNotRemoveContainerNetworkException {
        final NetworkConfig networkConfig = ContainerNetworkConfigBuilder.build(serviceInfo);
        final DockerHost host = (DockerHost) serviceInfo.getHost();
        final String networkId = networkClient.create(networkConfig, host);
        assertThat(networkId, is(notNullValue()));
        networkClient.checkNetwork(networkId, networkConfig, host);
        Thread.sleep(5000);
        networkClient.remove(networkId, host);
    }

}
