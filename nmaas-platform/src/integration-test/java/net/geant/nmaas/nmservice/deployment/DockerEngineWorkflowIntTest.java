package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineWorkflowIntTest {

	@Autowired
	@Qualifier("DockerEngine")
	private ContainerOrchestrator orchestrator;

	@Autowired
	private NmServiceRepositoryManager nmServiceRepositoryManager;

	@Autowired
	private DockerHostRepositoryManager dockerHostRepositoryManager;

	@Autowired
	private DockerNetworkRepositoryManager dockerNetworkRepositoryManager;

	private Identifier deploymentId = Identifier.newInstance("deploymentId");

	private Identifier clientId = Identifier.newInstance("clientId");

	@Before
	public void setup() throws DockerHostNotFoundException {
		final NmServiceInfo service = new NmServiceInfo(deploymentId, clientId, ITestHelper.alpineTomcatTemplate());
		final DockerHost dockerHost = dockerHostRepositoryManager.loadPreferredDockerHost();
		service.setHost(dockerHost);
		final DockerNetworkIpamSpec ipamSpec = new DockerNetworkIpamSpec("10.10.1.0/24", "10.10.1.254");
		final DockerContainerNetDetails testNetworkDetails1 = new DockerContainerNetDetails(8080, ipamSpec);
		final DockerContainerVolumesDetails testVolumeDetails1 = new DockerContainerVolumesDetails("/home/directory");
		final DockerContainer dockerContainer = new DockerContainer();
		dockerContainer.setNetworkDetails(testNetworkDetails1);
		dockerContainer.setVolumesDetails(testVolumeDetails1);
		service.setDockerContainer(dockerContainer);
		nmServiceRepositoryManager.storeService(service);
		final DockerNetwork dockerNetwork = new DockerNetwork(clientId, dockerHost, 100, "10.10.1.0/24", "10.10.1.254");
		dockerNetworkRepositoryManager.storeNetwork(dockerNetwork);
	}

	@Ignore
	@Test
	public void shouldDeployNewContainerWithDedicatedNetwork() throws
			ContainerOrchestratorInternalErrorException,
			CouldNotConnectToOrchestratorException,
			CouldNotPrepareEnvironmentException,
			CouldNotDeployNmServiceException,
			CouldNotRemoveNmServiceException,
			DockerNetworkCheckFailedException,
			ContainerCheckFailedException,
			InvalidDeploymentIdException,
			InterruptedException {
		// orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(serviceName);
		orchestrator.prepareDeploymentEnvironment(deploymentId);
		orchestrator.deployNmService(deploymentId);
		Thread.sleep(2000);
		orchestrator.checkService(deploymentId);
		assertThat(orchestrator.listServices(nmServiceRepositoryManager.loadService(deploymentId).getHost()),
				Matchers.hasItem(nmServiceRepositoryManager.loadService(deploymentId).getDockerContainer().getDeploymentId()));
		orchestrator.removeNmService(deploymentId);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(nmServiceRepositoryManager.loadService(deploymentId).getHost()),
				Matchers.not(Matchers.hasItem(nmServiceRepositoryManager.loadService(deploymentId).getDockerContainer().getDeploymentId())));
	}

	@After
	public void cleanServices() throws CouldNotConnectToOrchestratorException {
		try {
			orchestrator.removeNmService(deploymentId);
		} catch (CouldNotRemoveNmServiceException | ContainerOrchestratorInternalErrorException e) {
			// service was already removed
		}
	}

}
