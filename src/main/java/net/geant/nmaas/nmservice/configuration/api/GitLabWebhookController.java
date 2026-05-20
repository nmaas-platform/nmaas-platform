package net.geant.nmaas.nmservice.configuration.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.NmServiceDeployment;
import net.geant.nmaas.nmservice.configuration.exceptions.InvalidWebhookException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/gitlab/webhooks")
public class GitLabWebhookController {

    private final KubernetesRepositoryManager repositoryManager;
    private final NmServiceConfigurationProvider configurationProvider;

    @PostMapping("/{id}")
    public void triggerWebhook(@PathVariable String id) {
        try {
            log.info("Triggered webhook with id: {}", id);
            KubernetesNmServiceInfo service = repositoryManager.loadServiceByGitLabProjectWebhookId(id);
            if (service.getState().isOnline() || ServiceDeploymentState.VERIFICATION_FAILED.equals(service.getState())) {
                log.info("Triggering configuration reload for service: {}", service.getDescriptiveDeploymentId());
                configurationProvider.reloadNmService(NmServiceDeployment.builder()
                        .deploymentId(service.getDeploymentId())
                        .remoteCluster(service.getRemoteCluster())
                        .descriptiveDeploymentId(service.getDescriptiveDeploymentId())
                        .domainName(service.getDomain())
                        .build()
                );
            } else {
                log.info("Skipped configuration reload");
            }
        } catch (InvalidDeploymentIdException e) {
            throw new InvalidWebhookException(String.format("No service found for given webhook identifier (%s)", id));
        }
    }

}
