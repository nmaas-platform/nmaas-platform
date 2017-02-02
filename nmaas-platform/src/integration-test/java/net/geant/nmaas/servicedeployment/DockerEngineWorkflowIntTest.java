package net.geant.nmaas.servicedeployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostsRepository;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.servicedeployment.repository.NmServiceRepository;
import net.geant.nmaas.servicedeployment.repository.NmServiceTemplateRepository;
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
	private NmServiceRepository nmServicesRepository;

	@Autowired
	private DockerHostsRepository dockerHostsRepository;

	String serviceName = "tomcat-alpine";

	@Before
	public void setup() throws DockerHostNotFoundException {
		Long serviceIdentifier = System.nanoTime();
		DockerContainerSpec spec = new DockerContainerSpec(
				serviceName,
				serviceIdentifier,
				(DockerEngineContainerTemplate) templates.loadTemplate("tomcat-alpine"));
		spec.setClientDetails("client1", "company1");
		final ContainerNetworkIpamSpec ipamSpec = new ContainerNetworkIpamSpec(
				"10.10.1.0/24",
				"10.10.1.0/24",
				"10.10.1.254");
		final ContainerNetworkDetails testNetworkDetails1 = new ContainerNetworkDetails(ipamSpec, 123);
		final NmServiceInfo service = new NmServiceInfo(serviceName, NmServiceInfo.ServiceState.INIT, spec);
		service.setHost(dockerHostsRepository.loadPreferredDockerHost());
		service.setNetwork(testNetworkDetails1);
		nmServicesRepository.storeService(service);
	}

	@Test
	public void shouldDeployNewContainerWithDedicatedNetwork() throws
			OrchestratorInternalErrorException,
			CouldNotConnectToOrchestratorException,
			CouldNotPrepareEnvironmentException,
			CouldNotDeployNmServiceException,
			CouldNotCheckNmServiceStateException,
			CouldNotDestroyNmServiceException,
			InterruptedException,
			NmServiceRepository.ServiceNotFoundException {
		// orchestrator.verifyRequestObtainTargetAndNetworkDetails(serviceName);
		orchestrator.prepareDeploymentEnvironment(serviceName);
		orchestrator.deployNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.checkService(serviceName), Matchers.equalTo(NmServiceState.DEPLOYED));
		assertThat(orchestrator.listServices(nmServicesRepository.loadService(serviceName).getHost()),
				Matchers.hasItem(nmServicesRepository.loadService(serviceName).getDeploymentId()));
		orchestrator.removeNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(nmServicesRepository.loadService(serviceName).getHost()),
				Matchers.not(Matchers.hasItem(nmServicesRepository.loadService(serviceName).getDeploymentId())));
	}

	@After
	public void cleanServices() throws CouldNotConnectToOrchestratorException {
		System.out.println("Cleaning up ... removing containers.");
		try {
			orchestrator.removeNmService(serviceName);
		} catch (CouldNotDestroyNmServiceException | OrchestratorInternalErrorException e) {
			// service was already removed
		}
	}

}
