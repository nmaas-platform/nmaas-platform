package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.APP_INSTANCE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.BASE_URL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.DOMAIN_CODENAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.RELEASE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_HOSTNAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PASSWORD;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PORT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KubernetesDeploymentParametersProviderTest {

    private final KubernetesClusterDeploymentManager deploymentManager = mock(KubernetesClusterDeploymentManager.class);
    private final KubernetesClusterIngressManager ingressManager = mock(KubernetesClusterIngressManager.class);
    private final AppDeploymentRepository appDeploymentRepository = mock(AppDeploymentRepository.class);

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");

    private final KubernetesDeploymentParametersProvider provider = new KubernetesDeploymentParametersProvider(
            deploymentManager,
            ingressManager,
            appDeploymentRepository
    );

    @Test
    void shouldGenerateDeploymentParametersMap() {
        when(deploymentManager.getSMTPServerHostname()).thenReturn("smtpHostname");
        when(deploymentManager.getSMTPServerPort()).thenReturn(505);
        when(ingressManager.getExternalServiceDomain()).thenReturn("externalDomain");
        AppDeployment appDeployment = AppDeployment.builder()
                .deploymentId(DEPLOYMENT_ID)
                .deploymentName("deploymentName")
                .descriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"))
                .domain("domain")
                .build();
        when(appDeploymentRepository.findByDeploymentId(DEPLOYMENT_ID)).thenReturn(Optional.of(appDeployment));

        Map<String, String> deploymentParameters = provider.deploymentParameters(DEPLOYMENT_ID);

        assertThat(deploymentParameters)
                .isNotNull()
                .containsEntry(SMTP_HOSTNAME.name(), "smtpHostname")
                .containsEntry(SMTP_PORT.name(), "505")
                .containsEntry(BASE_URL.name(), "externalDomain")
                .containsEntry(DOMAIN_CODENAME.name(), "domain")
                .containsEntry(RELEASE_NAME.name(), "descriptiveDeploymentId")
                .containsEntry(APP_INSTANCE_NAME.name(), "deploymentname"); // note the lowercase conversion
        assertThat(deploymentParameters.get(SMTP_USERNAME.name())).isNull();
        assertThat(deploymentParameters.get(SMTP_PASSWORD.name())).isNull();
    }

}
