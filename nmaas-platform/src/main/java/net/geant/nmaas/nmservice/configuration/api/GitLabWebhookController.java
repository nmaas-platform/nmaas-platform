package net.geant.nmaas.nmservice.configuration.api;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.NmServiceDeployment;
import net.geant.nmaas.nmservice.configuration.exceptions.InvalidWebhookException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping("/api/gitlab/webhooks")
public class GitLabWebhookController {

    private KubernetesRepositoryManager repositoryManager;

    private NmServiceConfigurationProvider configurationProvider;

    @PostMapping("/{id}")
    public void triggerWebhook(@PathVariable String id) {
        try {
            log.info("Triggered webhook with id: " + id);
            KubernetesNmServiceInfo service = repositoryManager.loadServiceByGitLabProjectWebhookId(id);
            log.info("Service found: " + service.getDescriptiveDeploymentId());
            if (service.getState() != NmServiceDeploymentState.CONFIGURATION_INITIATED) {
                log.info("Triggering configuration reload");
                configurationProvider.reloadNmService(NmServiceDeployment.builder()
                        .deploymentId(service.getDeploymentId())
                        .descriptiveDeploymentId(service.getDescriptiveDeploymentId())
                        .domainName(service.getDomain())
                        .build()
                );
            }
        } catch (InvalidDeploymentIdException e) {
            throw new InvalidWebhookException(String.format("No service found for given webhook identifier (%s)", id));
        }
    }

}
