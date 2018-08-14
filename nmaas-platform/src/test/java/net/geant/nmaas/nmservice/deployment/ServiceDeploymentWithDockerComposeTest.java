package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class ServiceDeploymentWithDockerComposeTest {

	@Autowired
	private ContainerOrchestrator orchestrator;

	@MockBean
	private DockerComposeServiceRepositoryManager dockerComposeServiceRepositoryManager;

	@Test
	public void shouldInjectDockerComposeManager() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("DockerCompose"));
	}

	@Test
	public void shouldConfirmSupportForDeploymentOnDockerCompose() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.KUBERNETES, AppDeploymentEnv.DOCKER_COMPOSE));
		appDeploymentSpec.setDockerComposeFileTemplate(new DockerComposeFileTemplate());
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier.newInstance("did"), null, null, appDeploymentSpec);
	}

	@Test(expected = NmServiceRequestVerificationException.class)
	public void shouldNotifyIncompatibilityForDeploymentOnDockerCompose() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.KUBERNETES));
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(null, null, null, appDeploymentSpec);
	}

}
