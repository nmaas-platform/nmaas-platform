package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.webhooks.AppDeploymentWebhookDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
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
public class AppRemovalJob extends AppWebhookJob {

    @Autowired
    public AppRemovalJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper,
                         AppDeploymentRepositoryManager appDeploymentRepositoryManager, KubernetesRepositoryManager serviceInfoRepositoryManager, WebhookHistoryService webhookHistoryService, AutoWebhookTemplateService templateService) {
        super(restClient, webhookEventService, modelMapper, appDeploymentRepositoryManager, serviceInfoRepositoryManager, webhookHistoryService, templateService);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        final Long webhookId = dataMap.getLong("webhookId");
        final String deploymentId = dataMap.getString("deploymentId");

        try {
            final WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventTypeDto.APPLICATION_REMOVAL.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. AppRemovalJob is abandoned", webhookId);
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
            log.error("Failed to communicate with external system for the webhook of application removal with id {}", deploymentId);
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}

