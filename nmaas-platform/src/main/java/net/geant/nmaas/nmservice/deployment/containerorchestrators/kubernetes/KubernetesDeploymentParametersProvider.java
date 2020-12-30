package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.NoArgsConstructor;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.orchestration.AppDeploymentParametersProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@Profile("env_kubernetes")
@NoArgsConstructor
public class KubernetesDeploymentParametersProvider implements AppDeploymentParametersProvider {

    @Autowired
    private KClusterDeploymentManager deploymentManager;

    @Autowired
    private KClusterIngressManager ingressManager;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

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
        AppDeployment appDeployment = appDeploymentRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new IllegalStateException("Missing application deployment"));
        parametersMap.put(DOMAIN_CODENAME.name(), appDeployment.getDomain());
        parametersMap.put(RELEASE_NAME.name(), appDeployment.getDescriptiveDeploymentId().value());
        parametersMap.put(APP_INSTANCE_NAME.name(), appDeployment.getDeploymentName());
        return parametersMap;
    }

}
