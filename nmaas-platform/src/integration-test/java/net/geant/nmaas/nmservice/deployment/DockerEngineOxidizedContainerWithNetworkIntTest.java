package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerEngineOxidizedContainerWithNetworkIntTest {

	@Autowired
	@Qualifier("DockerEngine")
	private ContainerOrchestrationProvider orchestrator;

	@Autowired
	private NmServiceTemplateRepository templates;

	@Autowired
	private NmServiceRepository nmServiceRepository;

	@Autowired
	private DockerHostRepository dockerHostRepository;

	String serviceName = "demo-oxidized";

	@Before
	public void setup() throws DockerHostNotFoundException {
		DockerContainerSpec spec = new DockerContainerSpec(
				serviceName,
				(DockerEngineContainerTemplate) templates.loadTemplateByName("oxidized"));
		spec.setClientDetails("demo", "jra2t5");
		final ContainerNetworkIpamSpec ipamSpec = new ContainerNetworkIpamSpec(
				"192.168.239.0/24",
				"192.168.239.3");
		final ContainerNetworkDetails testNetworkDetails1 = new ContainerNetworkDetails(9000, ipamSpec, 239);
		final NmServiceInfo service = new NmServiceInfo(serviceName, NmServiceDeploymentState.INIT, spec);
		service.setHost(dockerHostRepository.loadPreferredDockerHost());
		service.setNetwork(testNetworkDetails1);
		service.setManagedDevicesIpAddresses(Arrays.asList("11.11.11.11", "22.22.22.22", "33.33.33.33", "44.44.44.44", "55.55.55.55"));
		service.setAppDeploymentId(UUID.randomUUID().toString());
		nmServiceRepository.storeService(service);
	}

	@Ignore
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
			ContainerCheckFailedException,
			NmServiceRequestVerificationException {
		// have to skip this to provide static VLAN number for data interface
		// orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(serviceName);
		orchestrator.prepareDeploymentEnvironment(serviceName);
		orchestrator.deployNmService(serviceName);
		Thread.sleep(2000);
		orchestrator.checkService(serviceName);
		assertThat(orchestrator.listServices(nmServiceRepository.loadService(serviceName).getHost()),
				Matchers.hasItem(nmServiceRepository.loadService(serviceName).getDeploymentId()));
	}

}
