package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryInit;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class DockerHostNetworkLifecycleManagerTest {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerHostNetworkRepositoryManager dockerHostNetworkRepositoryManager;
    @Autowired
    private DockerHostNetworkRepository dockerHostNetworkRepository;
    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;
    @Autowired
    private DockerNetworkLifecycleManager networkManager;

    @MockBean
    private DockerApiClient dockerApiClient;

    private Identifier deploymentId;
    private Identifier applicationId;
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier clientId2 = Identifier.newInstance("clientId2");
    private DockerHost dockerHost;

    @Before
    public void setup() throws DockerHostNotFoundException {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
        deploymentId = Identifier.newInstance("deploymentId");
        applicationId = Identifier.newInstance("applicationId");
        dockerHost = dockerHostRepositoryManager.loadPreferredDockerHost();
    }

    @After
    public void clean() {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
    }

    @Test
    public void shouldDeclareNewNetworkForClient() throws ContainerOrchestratorInternalErrorException, InvalidClientIdException, CouldNotRemoveContainerNetworkException {
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        DockerHostNetwork dockerHostNetwork = networkManager.networkForClient(clientId);
        assertThat(dockerHostNetwork, is(notNullValue()));
        assertThat(dockerHostNetwork.getClientId(), equalTo(clientId));
        assertThat(dockerHostNetwork.getHost(), equalTo(dockerHost));
        assertThat(dockerHostNetwork.getAssignedAddresses().isEmpty(), is(true));
        assertThat(dockerHostNetwork.getVlanNumber(), is(greaterThan(0)));
        assertThat(dockerHostNetwork.getSubnet().length(), is(greaterThan(0)));
        assertThat(dockerHostNetwork.getGateway().length(), is(greaterThan(0)));
        networkManager.removeNetwork(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
    }

    @Test
    public void shouldRemoveAllNetworksWhenHostRemoved() throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException, DockerHostInvalidException {
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        networkManager.declareNewNetworkForClientOnHost(clientId2, dockerHost);
        assertThat(dockerHostNetworkRepository.count(), is(2L));
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId2), is(true));
        dockerHostRepositoryManager.removeDockerHost(dockerHost.getName());
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId2), is(false));
        assertThat(dockerHostNetworkRepository.count(), is(0L));
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
        networkManager.removeNetwork(clientId);
        verify(dockerApiClient, times(1)).removeNetwork(Mockito.any(), Mockito.any());
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        assertThat(dockerHostNetworkRepository.count(), is(0L));
    }

    @Test
    public void shouldConnectContainerAndVerifyNetwork() throws InvalidClientIdException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException, DockerException, InterruptedException, CouldNotConnectContainerToNetworkException, CouldNotRemoveContainerNetworkException, InvalidDeploymentIdException, CouldNotCreateContainerNetworkException {
        when(dockerApiClient.createNetwork(Mockito.any(), Mockito.any())).thenReturn("testNetworkId");
        when(dockerApiClient.countContainersInNetwork(Mockito.any(), Mockito.any())).thenReturn(1);
        when(dockerApiClient.listNetworks(Mockito.any())).thenReturn(Arrays.asList("testNetworkId", "testNetworkId2"));
        final NmServiceInfo service = new NmServiceInfo(deploymentId, applicationId, clientId, new DockerContainerTemplate("image"));
        service.setHost(dockerHost);
        final DockerContainer dockerContainer = prepareTestContainer();
        service.setDockerContainer(dockerContainer);
        nmServiceRepositoryManager.storeService(service);
        networkManager.declareNewNetworkForClientOnHost(clientId, dockerHost);
        networkManager.deployNetworkForClient(clientId);
        networkManager.connectContainerToNetwork(clientId, dockerContainer);
        networkManager.verifyNetwork(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(true));
        assertThat(nmServiceRepositoryManager.loadAllRunningClientServices(clientId).isEmpty(), is(false));
        networkManager.disconnectContainerFromNetwork(clientId, dockerContainer);
        nmServiceRepositoryManager.notifyStateChange(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVED));
        assertThat(nmServiceRepositoryManager.loadAllRunningClientServices(clientId).isEmpty(), is(true));
        networkManager.removeNetwork(clientId);
        assertThat(networkManager.networkForClientAlreadyConfigured(clientId), is(false));
        nmServiceRepositoryManager.removeService(deploymentId);
    }

    private DockerContainer prepareTestContainer() {
        final DockerNetworkIpam ipamSpec = new DockerNetworkIpam("10.10.1.0/24", "10.10.1.254");
        final DockerContainerNetDetails testNetworkDetails1 = new DockerContainerNetDetails(8080, ipamSpec);
        final DockerContainerVolumesDetails testVolumeDetails1 = new DockerContainerVolumesDetails("/home/directory");
        final DockerContainer dockerContainer = new DockerContainer();
        dockerContainer.setNetworkDetails(testNetworkDetails1);
        dockerContainer.setVolumesDetails(testVolumeDetails1);
        dockerContainer.setDeploymentId("container1Deployed");
        return dockerContainer;
    }

}
