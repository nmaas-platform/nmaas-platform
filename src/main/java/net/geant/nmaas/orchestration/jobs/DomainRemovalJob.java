package net.geant.nmaas.orchestration.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.domain.DomainRemovalDto;
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
public class DomainRemovalJob extends WebhookJob {

    @Autowired
    public DomainRemovalJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper) {
        super(restClient, webhookEventService, modelMapper);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Started DomainRemovalJob ...");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        boolean hardRemoval = dataMap.getBoolean("hardRemoval");
        DomainView domain = (DomainView) dataMap.get("domain");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventType.DOMAIN_REMOVAL.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainRemovalJob is abandoned", webhookId);
                return;
            }

            callWebhook(webhook, new DomainRemovalDto(domain, hardRemoval));
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("Webhook does not exist. DomainRemovalJob is abandoned");
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhook of domain removal with id {}", domain.getId());
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}

