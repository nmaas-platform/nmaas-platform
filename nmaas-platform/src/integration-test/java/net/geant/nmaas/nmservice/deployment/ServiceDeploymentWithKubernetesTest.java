package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
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
	public void shouldConfirmSupportForDeploymentOnKubernetes() {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		AppDeployment appDeployment = appDeployment();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES));
		appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate());
		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier.newInstance("1"), appDeployment, appDeploymentSpec);
	}

	@Test
	public void shouldNotifyIncompatibilityForDeploymentOnKubernetes() {
		assertThrows(NmServiceRequestVerificationException.class, () -> {
			AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
			AppDeployment appDeployment = appDeployment();
			appDeploymentSpec.setSupportedDeploymentEnvironments(Collections.emptyList());
			appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate());
			orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(null, appDeployment, appDeploymentSpec);
		});
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
