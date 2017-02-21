package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceTemplateRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineWorkflowIntTest {

	@Autowired
	@Qualifier("DockerEngine")
	private ContainerOrchestrationProvider orchestrator;

	@Autowired
	private NmServiceTemplateRepository templates;

	@Autowired
	private NmServiceRepository nmServiceRepository;

	@Autowired
	private DockerHostRepository dockerHostRepository;

	String serviceName = "tomcat-alpine";

	@Before
	public void setup() throws DockerHostNotFoundException {
		DockerContainerSpec spec = new DockerContainerSpec(
				serviceName,
				(DockerEngineContainerTemplate) templates.loadTemplateByName("tomcat-alpine"));
		spec.setClientDetails("client1", "company1");
		final ContainerNetworkIpamSpec ipamSpec = new ContainerNetworkIpamSpec(
				"10.10.1.0/24",
				"10.10.1.254");
		final ContainerNetworkDetails testNetworkDetails1 = new ContainerNetworkDetails(8080, ipamSpec, 123);
		final NmServiceInfo service = new NmServiceInfo(serviceName, NmServiceDeploymentState.INIT, spec);
		service.setHost(dockerHostRepository.loadPreferredDockerHost());
		service.setNetwork(testNetworkDetails1);
		nmServiceRepository.storeService(service);
	}

	@Test
	public void shouldDeployNewContainerWithDedicatedNetwork() throws
			ContainerOrchestratorInternalErrorException,
			CouldNotConnectToOrchestratorException,
			CouldNotPrepareEnvironmentException,
			CouldNotDeployNmServiceException,
			CouldNotDestroyNmServiceException,
			InterruptedException,
			NmServiceRepository.ServiceNotFoundException,
			ContainerNetworkCheckFailedException,
			ContainerCheckFailedException {
		// orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(serviceName);
		orchestrator.prepareDeploymentEnvironment(serviceName);
		orchestrator.deployNmService(serviceName);
		Thread.sleep(2000);
		orchestrator.checkService(serviceName);
		assertThat(orchestrator.listServices(nmServiceRepository.loadService(serviceName).getHost()),
				Matchers.hasItem(nmServiceRepository.loadService(serviceName).getDeploymentId()));
		orchestrator.removeNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(nmServiceRepository.loadService(serviceName).getHost()),
				Matchers.not(Matchers.hasItem(nmServiceRepository.loadService(serviceName).getDeploymentId())));
	}

	@After
	public void cleanServices() throws CouldNotConnectToOrchestratorException {
		System.out.println("Cleaning up ... removing containers.");
		try {
			orchestrator.removeNmService(serviceName);
		} catch (CouldNotDestroyNmServiceException | ContainerOrchestratorInternalErrorException e) {
			// service was already removed
		}
	}

}
