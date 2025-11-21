package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import net.geant.nmaas.webhooks.AppDeploymentWebhookDto;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.security.GeneralSecurityException;

@Slf4j
@Component
public class AppDeploymentJob extends AppWebhookJob {

    @Autowired
    public AppDeploymentJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper,
                            AppDeploymentRepositoryManager appDeploymentRepositoryManager, KubernetesRepositoryManager serviceInfoRepositoryManager, WebhookHistoryService webhookHistoryService) {
        super(restClient, webhookEventService, modelMapper, appDeploymentRepositoryManager, serviceInfoRepositoryManager, webhookHistoryService);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        final Long webhookId = dataMap.getLong("webhookId");
        final String deploymentId = dataMap.getString("deploymentId");

        try {
            final WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventType.APPLICATION_DEPLOYMENT.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. AppDeploymentJob is abandoned", webhookId);
                return;
            }
            AppDeploymentWebhookDto webhookDto = getWebhookDto(deploymentId);
            callWebhook(webhook, webhookDto);
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("Webhook does not exist. AppDeploymentJob is abandoned");
        } catch (InvalidDeploymentIdException e) {
            log.warn("Application Deployment does not exist. AppDeploymentJob is abandoned");
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhook of application deployment with id {}", deploymentId);
            throw new JobExecutionException("Failed communication with external system");
        }
    }

}

