package net.geant.nmaas.orchestration.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.domain.DomainActionDto;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
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
    public DomainActionJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper) {
        super(restClient, webhookEventService, modelMapper);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Started DomainRemovalJob ...");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        String action = dataMap.getString("action");
        DomainView domain = (DomainView) dataMap.get("domain");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventType.DOMAIN_ACTION.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainActionJob is abandoned", webhookId);
                return;
            }

            callWebhook(webhook, new DomainActionDto(domain, action));
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

