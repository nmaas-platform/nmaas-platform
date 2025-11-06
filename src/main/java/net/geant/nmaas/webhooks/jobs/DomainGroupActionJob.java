package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.domain.DomainGroupView;
import net.geant.nmaas.webhooks.DomainGroupWebhookDto;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
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
    public DomainGroupActionJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper) {
        super(restClient, webhookEventService, modelMapper);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        String action = dataMap.getString("action");
        DomainGroupView domainGroup = (DomainGroupView) dataMap.get("domainGroup");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventType.DOMAIN_GROUP_ACTION.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainGroupJob is abandoned", webhookId);
                return;
            }
            DomainGroupWebhookDto view = new DomainGroupWebhookDto(domainGroup, action, WebhookEventType.DOMAIN_GROUP_ACTION);
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
