package net.geant.nmaas.nmservice.deployment;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryInit;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.helpers.DockerApiClientMockInit;
import net.geant.nmaas.helpers.DockerContainerTemplatesInit;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerEngineNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
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
@TestPropertySource("classpath:application-test-engine.properties")
public class DockerEngineWorkflowIntTest {

	@Autowired
	private ContainerOrchestrator orchestrator;
	@Autowired
	private DockerEngineServiceRepositoryManager nmServiceRepositoryManager;
	@Autowired
	private DockerHostRepositoryManager dockerHostRepositoryManager;
	@MockBean
	private ApplicationEventPublisher applicationEventPublisher;
	@MockBean
	private DockerApiClient dockerApiClient;

	private static final String DOMAIN = "domain";
	private static final String DEPLOYMENT_NAME = "deploymentName";
	private Identifier deploymentId = Identifier.newInstance("deploymentId");

	@Before
	public void setup() throws DockerException, InterruptedException {
		final DockerEngineNmServiceInfo service = new DockerEngineNmServiceInfo(deploymentId, DEPLOYMENT_NAME, DOMAIN, DockerContainerTemplatesInit.alpineTomcatTemplate());
		nmServiceRepositoryManager.storeService(service);
		DockerApiClientMockInit.mockMethods(dockerApiClient);
		DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
	}

	@After
	public void clear() throws InvalidDeploymentIdException, InvalidDomainException {
		nmServiceRepositoryManager.removeService(deploymentId);
		DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
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
