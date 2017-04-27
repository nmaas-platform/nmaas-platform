package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerNetworkManagerTest {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    @Autowired
    private DockerNetworkRepositoryManager dockerNetworkRepositoryManager;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Mock
    private DockerApiClient dockerApiClient;

    private Identifier deploymentId;

    private Identifier clientId;

    private DockerHost dockerHost;

    private DockerNetworkManager networkManager;

    @Before
    public void setup() throws DockerHostNotFoundException {
        deploymentId = Identifier.newInstance("deploymentId");
        clientId = Identifier.newInstance("clientId");
        dockerHost = dockerHostRepositoryManager.loadPreferredDockerHost();
        networkManager = new DockerNetworkManager(dockerNetworkRepositoryManager, dockerHostStateKeeper, dockerApiClient);
    }

    @Test
    public void shouldDeclareNewNetworkForClient() throws ContainerOrchestratorInternalErrorException, InvalidClientIdException {
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        assertThat(networkManager.networkForClient(clientId), is(notNullValue()));
        DockerNetwork dockerNetwork = networkManager.networkForClient(clientId);
        assertThat(dockerNetwork.getClientId(), equalTo(clientId));
        assertThat(dockerNetwork.getDockerHost(), equalTo(dockerHost));
        assertThat(dockerNetwork.getDockerContainers().isEmpty(), is(true));
        assertThat(dockerNetwork.getVlanNumber(), is(greaterThan(0)));
        assertThat(dockerNetwork.getSubnet().length(), is(greaterThan(0)));
        assertThat(dockerNetwork.getGateway().length(), is(greaterThan(0)));
        dockerNetworkRepositoryManager.removeNetwork(clientId);
    }

    @Test
    public void shouldDeployAndRemoveNewNetworkForClient()
            throws ContainerOrchestratorInternalErrorException, CouldNotCreateContainerNetworkException, DockerException, InterruptedException, DockerNetworkCheckFailedException, CouldNotRemoveContainerNetworkException {
        when(dockerApiClient.createNetwork(Mockito.any(), Mockito.any())).thenReturn("networkId");
        when(dockerApiClient.countContainersInNetwork(Mockito.any(), Mockito.any())).thenReturn(0);
        when(dockerApiClient.listNetworks(Mockito.any())).thenReturn(Arrays.asList("networkId"));
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        assertThat(networkManager.networkForClient(clientId).getDeploymentId(), is(nullValue()));
        networkManager.deployNetworkForClient(clientId);
        verify(dockerApiClient, times(1)).createNetwork(Mockito.any(), Mockito.any());
        assertThat(networkManager.networkForClient(clientId).getDeploymentId(), equalTo("networkId"));
        networkManager.verifyNetwork(clientId);
        networkManager.removeIfNoContainersAttached(clientId);
        verify(dockerApiClient, times(1)).removeNetwork(Mockito.any(), Mockito.any());
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
    }

    @Test
    public void shouldConnectContainerAndVerifyNetwork() throws InvalidClientIdException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException, DockerException, InterruptedException, CouldNotConnectContainerToNetworkException, CouldNotRemoveContainerNetworkException, InvalidDeploymentIdException {
        final NmServiceInfo service = new NmServiceInfo(deploymentId, clientId, new DockerContainerTemplate("image"));
        service.setHost(dockerHost);
        final DockerNetworkIpamSpec ipamSpec = new DockerNetworkIpamSpec("10.10.1.0/24", "10.10.1.254");
        final DockerContainerNetDetails testNetworkDetails1 = new DockerContainerNetDetails(8080, ipamSpec);
        final DockerContainerVolumesDetails testVolumeDetails1 = new DockerContainerVolumesDetails("/home/directory");
        final DockerContainer dockerContainer = new DockerContainer();
        dockerContainer.setNetworkDetails(testNetworkDetails1);
        dockerContainer.setVolumesDetails(testVolumeDetails1);
        dockerContainer.setDeploymentId("container1Deployed");
        service.setDockerContainer(dockerContainer);
        nmServiceRepositoryManager.storeService(service);
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        networkManager.connectContainerToNetwork(clientId, dockerContainer);
        when(dockerApiClient.countContainersInNetwork(Mockito.any(), Mockito.any())).thenReturn(1);
        networkManager.verifyNetwork(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        networkManager.removeIfNoContainersAttached(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        networkManager.disconnectContainerFromNetwork(clientId, dockerContainer.getDeploymentId());
        networkManager.removeIfNoContainersAttached(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        nmServiceRepositoryManager.removeService(deploymentId);
    }

}
