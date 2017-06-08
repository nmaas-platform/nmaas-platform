package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkManager;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotCreateContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveContainerNetworkException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineContainerNetworkIntTest {

    @Autowired
    private DockerNetworkManager dockerNetworkManager;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() {
    }

    @Ignore
    @Test
    public void shouldCreateInspectAndRemoteSimpleNetwork() throws
            DockerHostNotFoundException,
            CouldNotCreateContainerNetworkException,
            CouldNotRemoveContainerNetworkException,
            ContainerOrchestratorInternalErrorException,
            InterruptedException {
        dockerNetworkManager.declareNewNetworkForClientOnHost(clientId, dockerHostRepositoryManager.loadPreferredDockerHost());
        dockerNetworkManager.deployNetworkForClient(clientId);
        assertThat(dockerNetworkManager.networkForClient(clientId).getDeploymentId(), is(notNullValue()));
        Thread.sleep(5000);
        dockerNetworkManager.removeIfNoContainersAttached(clientId);
    }

}
