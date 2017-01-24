package net.geant.nmaas.servicedeployment;

import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerEngineContainerTemplate;
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
public class DockerEngineContainerIntTest {

	@Autowired
	@Qualifier("DockerEngine")
	private ContainerOrchestrationProvider orchestrator;

	@Autowired
	private NmServiceTemplateRepository templates;

	@Autowired
	private NmServiceRepository nmServicesRepository;

	String serviceName = "tomcat-alpine";

	@Before
	public void setup(){
		Long serviceIdentifier = System.nanoTime();
		DockerContainerSpec spec = new DockerContainerSpec(serviceName, serviceIdentifier, (DockerEngineContainerTemplate) templates.loadTemplate("tomcat-alpine"));
		NmServiceInfo service = new NmServiceInfo(serviceName, NmServiceInfo.ServiceState.INIT, spec);
		nmServicesRepository.storeService(service);
	}

	@Test
	public void shouldDeployNewContainer()
			throws OrchestratorInternalErrorException, CouldNotDeployNmServiceException, CouldNotDestroyNmServiceException, CouldNotConnectToOrchestratorException, NmServiceNotFoundException, UnknownInternalException, InterruptedException, NmServiceRepository.ServiceNotFoundException {
		orchestrator.deployNmService(serviceName);
		Thread.sleep(10000);
		assertThat(orchestrator.listServices(),
				Matchers.hasItem(nmServicesRepository.loadService(serviceName).getSpec().uniqueDeploymentName()));
		orchestrator.removeNmService(serviceName);
		Thread.sleep(2000);
		assertThat(orchestrator.listServices(),
				Matchers.not(Matchers.hasItem(nmServicesRepository.loadService(serviceName).getSpec().uniqueDeploymentName())));
	}

	@After
	public void cleanServices() {
		System.out.println("Cleaning up ... removing containers.");
		try {
			orchestrator.removeNmService(serviceName);
		} catch (CouldNotDestroyNmServiceException
				| NmServiceNotFoundException
				| CouldNotConnectToOrchestratorException
				| OrchestratorInternalErrorException e) {
			System.out.println(e.getMessage());
		}
	}

}
