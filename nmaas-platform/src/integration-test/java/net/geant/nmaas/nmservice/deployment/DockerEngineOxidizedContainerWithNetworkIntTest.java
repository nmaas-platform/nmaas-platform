package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineOxidizedContainerWithNetworkIntTest {

	@Autowired
	@Qualifier("DockerEngine")
	private ContainerOrchestrator orchestrator;

	@Autowired
	private NmServiceRepositoryManager nmServiceRepositoryManager;

	@Autowired
	private DockerHostRepositoryManager dockerHostRepositoryManager;

	private Identifier deploymentId = Identifier.newInstance("deploymentId");
	private Identifier applicationId = Identifier.newInstance("applicationId");
	private Identifier clientId = Identifier.newInstance("clientId");

	@Before
	public void setup() throws DockerHostNotFoundException {
		final DockerNetworkIpamSpec ipamSpec = new DockerNetworkIpamSpec("192.168.239.0/24", "192.168.239.3");
		final NmServiceInfo service = new NmServiceInfo(deploymentId, applicationId, clientId, ITestHelper.oxidizedTemplate());
		service.setHost(dockerHostRepositoryManager.loadPreferredDockerHost());
		final DockerContainerNetDetails testNetworkDetails1 = new DockerContainerNetDetails(9000, ipamSpec);
		final DockerContainer dockerContainer = new DockerContainer();
		dockerContainer.setNetworkDetails(testNetworkDetails1);
		service.setManagedDevicesIpAddresses(Arrays.asList("11.11.11.11", "22.22.22.22", "33.33.33.33", "44.44.44.44", "55.55.55.55"));
		service.setDeploymentId(deploymentId);
		nmServiceRepositoryManager.storeService(service);
	}

	@Ignore
	@Test
	public void shouldDeployNewContainerWithDedicatedNetwork() throws
			NmServiceRequestVerificationException,
			ContainerOrchestratorInternalErrorException,
			CouldNotPrepareEnvironmentException,
			CouldNotDeployNmServiceException,
			DockerNetworkCheckFailedException,
			ContainerCheckFailedException,
			InvalidDeploymentIdException,
			InterruptedException {
		orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(deploymentId);
		orchestrator.prepareDeploymentEnvironment(deploymentId);
		orchestrator.deployNmService(deploymentId);
		Thread.sleep(2000);
		orchestrator.checkService(deploymentId);
		assertThat(orchestrator.listServices(nmServiceRepositoryManager.loadService(deploymentId).getHost()),
				Matchers.hasItem(nmServiceRepositoryManager.loadService(deploymentId).getDockerContainer().getDeploymentId()));
	}

}
