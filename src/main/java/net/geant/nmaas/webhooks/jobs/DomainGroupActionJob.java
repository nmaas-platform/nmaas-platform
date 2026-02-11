package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.webhooks.DomainGroupWebhookDto;
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
public class DomainGroupActionJob extends WebhookJob {

    @Autowired
    public DomainGroupActionJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper, WebhookHistoryService webhookHistoryService, AutoWebhookTemplateService templateService) {
        super(restClient, webhookEventService, modelMapper, webhookHistoryService, templateService);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        String action = dataMap.getString("action");
        DomainGroupDto domainGroup = (DomainGroupDto) dataMap.get("domainGroup");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventTypeDto.DOMAIN_GROUP_ACTION.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainGroupJob is abandoned", webhookId);
                return;
            }
            DomainGroupWebhookDto view = new DomainGroupWebhookDto(domainGroup, action, WebhookEventTypeDto.DOMAIN_GROUP_ACTION);
            callWebhook(webhook, view);
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("Webhook does not exist. DomainGroupJob is abandoned");
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhoook of domain group with id {}", domainGroup.getId());
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}
