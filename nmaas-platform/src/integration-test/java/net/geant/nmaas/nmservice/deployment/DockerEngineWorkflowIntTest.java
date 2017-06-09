package net.geant.nmaas.nmservice.deployment;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.helpers.DockerApiClientMockInit;
import net.geant.nmaas.helpers.DockerContainerTemplatesInit;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class DockerEngineWorkflowIntTest {

	@Autowired
	private ContainerOrchestrator orchestrator;

	@Autowired
	private NmServiceRepositoryManager nmServiceRepositoryManager;
	@Autowired
	private DockerHostRepositoryManager dockerHostRepositoryManager;

	@MockBean
	private ApplicationEventPublisher applicationEventPublisher;
	@MockBean
	private DockerApiClient dockerApiClient;

	private Identifier deploymentId = Identifier.newInstance("deploymentId");
	private Identifier applicationId = Identifier.newInstance("applicationId");
	private Identifier clientId = Identifier.newInstance("clientId");

	@Before
	public void setup() throws DockerHostNotFoundException, DockerException, InterruptedException {
		final NmServiceInfo service = new NmServiceInfo(deploymentId, applicationId, clientId, DockerContainerTemplatesInit.alpineTomcatTemplate());
		nmServiceRepositoryManager.storeService(service);
		DockerApiClientMockInit.mockMethods(dockerApiClient);
	}

	@After
	public void clear() throws InvalidDeploymentIdException, InvalidClientIdException {
		nmServiceRepositoryManager.removeService(deploymentId);
	}

	@Test
	public void shouldDeployNewContainerWithDedicatedNetwork() throws
			NmServiceRequestVerificationException,
			ContainerOrchestratorInternalErrorException,
			CouldNotConnectToOrchestratorException,
			CouldNotPrepareEnvironmentException,
			CouldNotDeployNmServiceException,
			CouldNotRemoveNmServiceException,
			DockerNetworkCheckFailedException,
			ContainerCheckFailedException,
			InvalidDeploymentIdException {
		orchestrator.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
		orchestrator.prepareDeploymentEnvironment(deploymentId);
		orchestrator.deployNmService(deploymentId);
		orchestrator.checkService(deploymentId);
		orchestrator.removeNmService(deploymentId);
	}

}
