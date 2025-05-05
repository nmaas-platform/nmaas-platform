package net.geant.nmaas.orchestration.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.WebhookEventService;
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
public class DomainCreationJob extends WebhookJob {
    private final DomainService domainService;

    @Autowired
    public DomainCreationJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper, DomainService domainService){
        super(restClient, webhookEventService, modelMapper);
        this.domainService = domainService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        Long domainId = dataMap.getLong("domainId");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventType.DOMAIN_CREATION.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. DomainCreationJob is abandoned", webhookId);
                return;
            }

            Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(String.format("Domain with id: %d cannot be found", domainId)));
            DomainView view = modelMapper.map(domain, DomainView.class);

            callWebhook(webhook, view);
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("Webhook or domain does not exist. DomainCreationJob is abandoned");
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhoook of domain creation with id {}", domainId);
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}
