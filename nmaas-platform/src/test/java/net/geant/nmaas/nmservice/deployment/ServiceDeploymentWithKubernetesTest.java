package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class ServiceDeploymentWithKubernetesTest {

	@Autowired
	private ContainerOrchestrator orchestrator;

	@Test
	public void shouldInjectKubernetesManager() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("Kubernetes"));
	}

	@Test
	public void shouldConfirmSupportForDeploymentOnKubernetes() throws NmServiceRequestVerificationException {
		orchestrator.verifyDeploymentEnvironmentSupport(Arrays.asList(AppDeploymentEnv.KUBERNETES, AppDeploymentEnv.DOCKER_COMPOSE));
	}

	@Test(expected = NmServiceRequestVerificationException.class)
	public void shouldNotifyIncompatibilityForDeploymentOnKubernetes() throws NmServiceRequestVerificationException {
		orchestrator.verifyDeploymentEnvironmentSupport(Arrays.asList(AppDeploymentEnv.DOCKER_COMPOSE, AppDeploymentEnv.DOCKER_ENGINE));
	}

}
