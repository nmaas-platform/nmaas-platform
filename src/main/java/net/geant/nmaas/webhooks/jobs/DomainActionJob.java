package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.webhooks.DomainActionDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
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
public class DomainActionJob extends WebhookJob {

    @Autowired
    public DomainActionJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper, WebhookHistoryService webhookHistoryService, AutoWebhookTemplateService templateService) {
        super(restClient, webhookEventService, modelMapper, webhookHistoryService, templateService);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Started DomainActionJob ...");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        String action = dataMap.getString("action");
        DomainBaseDto domain = (DomainBaseDto) dataMap.get("domain");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventTypeDto.DOMAIN_ACTION.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainActionJob is abandoned", webhookId);
                return;
            }

            callWebhook(webhook, new DomainActionDto(domain, action, WebhookEventTypeDto.DOMAIN_ACTION));
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("Webhook does not exist. DomainActionJob is abandoned");
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhook of domain action with id {}", domain.getId());
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}

