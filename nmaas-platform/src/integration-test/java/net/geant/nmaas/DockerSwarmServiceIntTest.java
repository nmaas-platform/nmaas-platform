package net.geant.nmaas;

import net.geant.nmaas.exception.*;
import net.geant.nmaas.orchestrators.dockerswarm.NmServiceDockerSwarmSpec;
import net.geant.nmaas.repository.NmServiceTemplateRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerSwarmServiceIntTest {

	@Autowired
	@Qualifier("DockerSwarm")
	private ContainerOrchestrationProvider orchestrator;

	@Autowired
	private NmServiceTemplateRepository templates;

	@Test
	public void shouldDeployNewService()
			throws OrchestratorInternalErrorException, CouldNotDeployNmServiceException, CouldNotDestroyNmServiceException, CouldNotConnectToOrchestratorException, NmServiceNotFoundException, UnknownInternalException, InterruptedException {
		String serviceName = "tomcat-on-alpine-test-5";
		NmServiceDockerSwarmSpec spec = new NmServiceDockerSwarmSpec(serviceName);
		orchestrator.deployNmService(templates.loadTemplate("tomcat-on-alpine"), spec);
		Thread.sleep(5000);
		assertThat(orchestrator.listServices(), Matchers.hasItem(serviceName));
		orchestrator.destroyNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(), Matchers.not(Matchers.hasItem(serviceName)));
	}

	@After
	public void cleanServices() {
		System.out.println("Cleaning up ... removing services.");
		try {
			String serviceName = "tomcat-on-alpine-test-5";
			orchestrator.destroyNmService(serviceName);
		} catch (CouldNotDestroyNmServiceException
				| NmServiceNotFoundException
				| CouldNotConnectToOrchestratorException
				| OrchestratorInternalErrorException e) {
			System.out.println(e.getMessage());
		}
	}

}
