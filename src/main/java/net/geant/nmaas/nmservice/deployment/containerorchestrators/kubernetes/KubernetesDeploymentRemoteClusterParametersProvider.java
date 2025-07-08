package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.APP_INSTANCE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.BASE_URL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.DOMAIN_CODENAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.RELEASE_NAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_FROM_DEFAULT_DOMAIN;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_HOSTNAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_HOST_WITH_PORT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PASSWORD;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_PORT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType.SMTP_USERNAME;

@Component
@RequiredArgsConstructor
public class KubernetesDeploymentRemoteClusterParametersProvider {

    private final AppDeploymentRepository appDeploymentRepository;

    public Map<String, String> deploymentRemoteParameters(Identifier deploymentId, KCluster cluster) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SMTP_HOSTNAME.name(), cluster.getDeployment().getSmtpServerHostname());
        parametersMap.put(SMTP_PORT.name(), cluster.getDeployment().getSmtpServerPort().toString());
        parametersMap.put(SMTP_HOST_WITH_PORT.name(), cluster.getDeployment().getSmtpServerHostname() + ":" + cluster.getDeployment().getSmtpServerPort().toString());
        parametersMap.put(SMTP_FROM_DEFAULT_DOMAIN.name(), cluster.getDeployment().getSmtpFromDefaultDomain());
        cluster.getDeployment().getSMTPServerUsername().ifPresent(username -> {
            if (!username.isEmpty()) {
                parametersMap.put(SMTP_USERNAME.name(), username);
            }
        });
        cluster.getDeployment().getSMTPServerPassword().ifPresent(value -> {
            if (!value.isEmpty()) {
                parametersMap.put(SMTP_PASSWORD.name(), value);
            }
        });
        parametersMap.put(BASE_URL.name(), cluster.getIngress().getExternalServiceDomain());
        var appDeployment = appDeploymentRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new IllegalStateException("Missing application deployment"));
        parametersMap.put(DOMAIN_CODENAME.name(), appDeployment.getDomain());
        parametersMap.put(RELEASE_NAME.name(), appDeployment.getDescriptiveDeploymentId().value());
        parametersMap.put(APP_INSTANCE_NAME.name(), appDeployment.getDeploymentName().toLowerCase());
        return parametersMap;
    }

}
