package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmServiceSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceTemplateRepository;
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

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerSwarmWorkflowIntTest {

	@Autowired
	@Qualifier("DockerSwarm")
	private ContainerOrchestrationProvider orchestrator;

	@Autowired
	private NmServiceTemplateRepository templates;

	@Autowired
	private NmServiceRepository nmServicesRepository;

	String serviceName = "tomcat-alpine";

	@Before
	public void setup(){
		Long serviceIdentifier = System.nanoTime();
		final DockerSwarmNmServiceTemplate template = (DockerSwarmNmServiceTemplate) templates.loadTemplateByName("tomcat-on-swarm-alpine");
		final DockerSwarmServiceSpec spec = new DockerSwarmServiceSpec(serviceName, template);
		NmServiceInfo service = new NmServiceInfo(serviceName, NmServiceDeploymentState.INIT, spec);
		nmServicesRepository.storeService(service);
	}

	/**
	 * This test verifies correct communication with Docker Swarm manager and basic workflow execution.
	 * It needs to be @Ignored since it assumes that a remote manager is running.
	 */
	@Ignore
	@Test
	public void shouldDeployNewService() throws
            ContainerOrchestratorInternalErrorException,
            CouldNotDeployNmServiceException,
            CouldNotDestroyNmServiceException,
            CouldNotConnectToOrchestratorException,
            NmServiceNotFoundException,
			InterruptedException,
			NmServiceRepository.ServiceNotFoundException {
		orchestrator.deployNmService(serviceName);
		Thread.sleep(5000);
		assertThat(orchestrator.listServices(nmServicesRepository.loadService(serviceName).getHost()),
				Matchers.hasItem(nmServicesRepository.loadService(serviceName).getAppDeploymentId()));
		orchestrator.removeNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(nmServicesRepository.loadService(serviceName).getHost()),
				Matchers.not(nmServicesRepository.loadService(serviceName).getAppDeploymentId()));
	}

	@After
	public void cleanServices() throws CouldNotConnectToOrchestratorException {
		System.out.println("Cleaning up ... removing services.");
		try {
			orchestrator.removeNmService(serviceName);
		} catch (CouldNotDestroyNmServiceException | ContainerOrchestratorInternalErrorException e) {
			// service was already removed
		}
	}

}