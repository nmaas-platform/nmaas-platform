package net.geant.nmaas.nmservice.deployment;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkRepositoryManager;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotCreateContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveContainerNetworkException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineContainerNetworkIntTest {

    @Autowired
    private DockerNetworkManager dockerNetworkManager;
    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerNetworkRepositoryManager dockerNetworkRepositoryManager;

    @MockBean
    private DockerApiClient dockerApiClient;

    private Identifier clientId = Identifier.newInstance("clientId");
    private String dockerNetworkId = "dockerNetworkId";

    @Before
    public void setup() throws DockerException, InterruptedException {
        when(dockerApiClient.createNetwork(any(), any())).thenReturn(dockerNetworkId);
    }

    @Test
    public void shouldCreateInspectAndRemoteSimpleNetwork() throws
            DockerHostNotFoundException,
            CouldNotCreateContainerNetworkException,
            CouldNotRemoveContainerNetworkException,
            ContainerOrchestratorInternalErrorException,
            InterruptedException, InvalidClientIdException {
        dockerNetworkManager.declareNewNetworkForClientOnHost(clientId, dockerHostRepositoryManager.loadPreferredDockerHost());
        assertThat(dockerNetworkRepositoryManager.checkNetwork(clientId), is(true));
        assertThat(
                dockerNetworkRepositoryManager.loadNetwork(clientId).getDockerHost().getName(),
                equalTo(dockerHostRepositoryManager.loadPreferredDockerHost().getName()));
        dockerNetworkManager.deployNetworkForClient(clientId);
        assertThat(
                dockerNetworkRepositoryManager.loadNetwork(clientId).getDeploymentId(),
                equalTo(dockerNetworkId));
        dockerNetworkManager.removeIfNoContainersAttached(clientId);
        assertThat(dockerNetworkRepositoryManager.checkNetwork(clientId), is(false));
    }

}
