package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class ServiceDeploymentWithDockerEngineTest {

	@Autowired
	private ContainerOrchestrator orchestrator;

	@MockBean
	private DockerEngineServiceRepositoryManager dockerEngineServiceRepositoryManager;

	@Test
	public void shouldInjectDockerEngineManager() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("DockerEngine"));
	}

	@Test
	public void shouldConfirmSupportForDeploymentOnDockerEngine() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.DOCKER_ENGINE, AppDeploymentEnv.DOCKER_COMPOSE));
		appDeploymentSpec.setDockerContainerTemplate(new DockerContainerTemplate());
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier.newInstance("id"), null, null, appDeploymentSpec);
	}

	@Test(expected = NmServiceRequestVerificationException.class)
	public void shouldNotifyIncompatibilityForDeploymentOnDockerEngine() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.DOCKER_COMPOSE, AppDeploymentEnv.KUBERNETES));
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(null, null, null, appDeploymentSpec);
	}

}
