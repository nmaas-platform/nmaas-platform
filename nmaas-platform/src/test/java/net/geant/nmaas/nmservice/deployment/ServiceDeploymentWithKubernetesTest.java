package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceDeploymentWithKubernetesTest {

	@Autowired
	private ContainerOrchestrator orchestrator;
	@MockBean
	private KubernetesRepositoryManager kubernetesRepositoryManager;
	@MockBean
	private IngressResourceManager ingressResourceManager;

	@Test
	public void shouldInjectKubernetesManager() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("Kubernetes"));
	}

	@Test
	public void shouldConfirmSupportForDeploymentOnKubernetes() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		AppDeployment appDeployment = appDeployment();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES));
		appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate());
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier.newInstance("1"), appDeployment, appDeploymentSpec);
	}

	@Test(expected = NmServiceRequestVerificationException.class)
	public void shouldNotifyIncompatibilityForDeploymentOnKubernetes() throws NmServiceRequestVerificationException {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		AppDeployment appDeployment = appDeployment();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Collections.emptyList());
		appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate());
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(null, appDeployment, appDeploymentSpec);
	}

	private AppDeployment appDeployment() {
		return AppDeployment.builder()
				.deploymentId(Identifier.newInstance(1L))
				.domain("domain")
				.applicationId(Identifier.newInstance("app"))
				.deploymentName("deploy")
				.configFileRepositoryRequired(false)
				.storageSpace(20).build();
	}

}
