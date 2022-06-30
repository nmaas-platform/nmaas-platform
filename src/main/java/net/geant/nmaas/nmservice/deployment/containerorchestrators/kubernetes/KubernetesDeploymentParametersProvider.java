package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.orchestration.AppDeploymentParametersProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.APP_INSTANCE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.BASE_URL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.DOMAIN_CODENAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.RELEASE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_HOSTNAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PASSWORD;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PORT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_USERNAME;

@Component
@RequiredArgsConstructor
@Profile("env_kubernetes")
public class KubernetesDeploymentParametersProvider implements AppDeploymentParametersProvider {

    private final KubernetesClusterDeploymentManager deploymentManager;
    private final KubernetesClusterIngressManager ingressManager;
    private final AppDeploymentRepository appDeploymentRepository;

    @Override
    public Map<String, String> deploymentParameters(Identifier deploymentId) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SMTP_HOSTNAME.name(), deploymentManager.getSMTPServerHostname());
        parametersMap.put(SMTP_PORT.name(), deploymentManager.getSMTPServerPort().toString());
        deploymentManager.getSMTPServerUsername().ifPresent(username -> {
            if(!username.isEmpty()) {
                parametersMap.put(SMTP_USERNAME.name(), username);
            }
        });
        deploymentManager.getSMTPServerPassword().ifPresent(value -> {
            if(!value.isEmpty()) {
                parametersMap.put(SMTP_PASSWORD.name(), value);
            }
        });
        parametersMap.put(BASE_URL.name(), ingressManager.getExternalServiceDomain());
        var appDeployment = appDeploymentRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new IllegalStateException("Missing application deployment"));
        parametersMap.put(DOMAIN_CODENAME.name(), appDeployment.getDomain());
        parametersMap.put(RELEASE_NAME.name(), appDeployment.getDescriptiveDeploymentId().value());
        parametersMap.put(APP_INSTANCE_NAME.name(), appDeployment.getDeploymentName().toLowerCase());
        return parametersMap;
    }

}
