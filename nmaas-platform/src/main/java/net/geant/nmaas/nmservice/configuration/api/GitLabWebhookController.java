package net.geant.nmaas.nmservice.configuration.api;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.exceptions.InvalidWebhookException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/gitlab/webhooks")
public class GitLabWebhookController {

    private KubernetesRepositoryManager repositoryManager;

    @Autowired
    public GitLabWebhookController(KubernetesRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @PostMapping("/{id}")
    public void triggerWebhook(@PathVariable String id) {
        // TODO complete webhook handling
        try {
            log.info("Triggered webhook with id: " + id);
            KubernetesNmServiceInfo service = repositoryManager.loadServiceByGitLabProjectWebhookId(id);
            log.info("Service found: " + service.getDescriptiveDeploymentId());
        } catch (InvalidDeploymentIdException e) {
            throw new InvalidWebhookException(String.format("No service found for given webhook identifier (%s)", id));
        }
    }

}
