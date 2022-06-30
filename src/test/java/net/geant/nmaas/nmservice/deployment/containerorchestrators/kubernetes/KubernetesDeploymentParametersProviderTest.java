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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesDeploymentParametersProviderTest {

    private final KubernetesClusterDeploymentManager deploymentManager = mock(KubernetesClusterDeploymentManager.class);
    private final KubernetesClusterIngressManager ingressManager = mock(KubernetesClusterIngressManager.class);
    private final AppDeploymentRepository appDeploymentRepository = mock(AppDeploymentRepository.class);

    private final static Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");

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

        assertThat(deploymentParameters).isNotNull();
        assertThat(deploymentParameters.get(SMTP_HOSTNAME.name())).isEqualTo("smtpHostname");
        assertThat(deploymentParameters.get(SMTP_PORT.name())).isEqualTo("505");
        assertThat(deploymentParameters.get(SMTP_USERNAME.name())).isNull();
        assertThat(deploymentParameters.get(SMTP_PASSWORD.name())).isNull();
        assertThat(deploymentParameters.get(BASE_URL.name())).isEqualTo("externalDomain");
        assertThat(deploymentParameters.get(DOMAIN_CODENAME.name())).isEqualTo("domain");
        assertThat(deploymentParameters.get(RELEASE_NAME.name())).isEqualTo("descriptiveDeploymentId");
        // note the lowercase conversion
        assertThat(deploymentParameters.get(APP_INSTANCE_NAME.name())).isEqualTo("deploymentname");
    }

}
