package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.*;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ServiceDeploymentWithKubernetesTest {

	@Autowired
	private ContainerOrchestrator orchestrator;

	@Autowired
	private KubernetesRepositoryManager repositoryManager;

	private static final Identifier deploymentId = Identifier.newInstance(1L);

	@AfterEach
	void cleanupNmServiceInfoRepository() {
		repositoryManager.removeAllServices();
	}

	@Test
	void shouldInjectKubernetesManager() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("Kubernetes"));
	}

	@Test
	void shouldConfirmSupportForDeploymentOnKubernetes() {
		AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
		AppDeployment appDeployment = appDeployment();
		appDeploymentSpec.setSupportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES));
		appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate(
				null,
				new KubernetesChart(null, "test", "0.0.0"),
				"archive",
				null,
				new HelmChartRepositoryEmbeddable("test", "http://test")
		));
		appDeploymentSpec.setStorageVolumes(Collections.singleton(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)));
		appDeploymentSpec.setAccessMethods(Collections.singleton(new AppAccessMethod(ServiceAccessMethodType.DEFAULT, "name", "tag", null)));

		orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier.newInstance(1L), appDeployment, appDeploymentSpec);
		KubernetesNmServiceInfo info = repositoryManager.loadService(deploymentId);

		assertThat(info, is(notNullValue()));
		assertThat(info.getDeploymentId(), equalTo(appDeployment.getDeploymentId()));
		assertThat(info.getDeploymentName(), equalTo(appDeployment.getDeploymentName()));
		assertThat(info.getDomain(), equalTo(appDeployment.getDomain()));
		assertThat(info.getDescriptiveDeploymentId().getValue(), equalTo("domain-appname-100"));
	}

	@Test
	void shouldNotifyIncompatibilityForDeploymentOnKubernetes() {
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
				.instanceId(100L)
				.deploymentId(deploymentId)
				.descriptiveDeploymentId(new Identifier("domain-appname-100"))
				.domain("domain")
				.applicationId(Identifier.newInstance("appId"))
				.deploymentName("deploy")
				.configFileRepositoryRequired(false)
				.appName("AppName").build();
	}

}
