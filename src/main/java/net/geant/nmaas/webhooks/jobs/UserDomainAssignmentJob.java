package net.geant.nmaas.webhooks.jobs;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserView;
import net.geant.nmaas.api.dto.webhooks.UserDomainAssignmentWebhookDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
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
public class UserDomainAssignmentJob extends WebhookJob {

    private final DomainService domainService;
    private final UserService userService;

    @Autowired
    public UserDomainAssignmentJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper, WebhookHistoryService webhookHistoryService, DomainService domainService, UserService userService, AutoWebhookTemplateService templateService) {
        super(restClient, webhookEventService, modelMapper, webhookHistoryService, templateService);
        this.domainService = domainService;
        this.userService = userService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long webhookId = dataMap.getLong("webhookId");
        Long domainId = dataMap.getLong("domainId");
        Long userId = dataMap.getLong("userId");
        Role role = Role.valueOf(dataMap.getString("role"));
        String action = dataMap.getString("action");

        try {
            WebhookEventDto webhook = webhookEventService.getById(webhookId);
            if (!WebhookEventTypeDto.USER_ASSIGNMENT.equals(webhook.getEventType())) {
                log.warn("Webhook's event type with id {} has been updated. UserDomainAssignmentJob is abandoned", webhookId);
                return;
            }
            Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(String.format("Domain with id: %d cannot be found", domainId)));
            User user = userService.findById(userId).orElseThrow(() -> new MissingElementException(String.format("User with id: %d cannot be found", userId)));
            UserView userView = modelMapper.map(user, UserView.class);
            userView.setSshKeys(null);

            UserDomainAssignmentWebhookDto dto = new UserDomainAssignmentWebhookDto(userView, modelMapper.map(domain, DomainBaseDto.class), RoleDto.valueOf(role.name()), action, WebhookEventTypeDto.USER_ASSIGNMENT);
            callWebhook(webhook, dto);
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt webhook with id {}", webhookId);
            throw new JobExecutionException("Failed webhook decryption");
        } catch (MissingElementException e) {
            log.warn("{} UserDomainAssignmentJob is abandoned", e.getMessage());
        } catch (WebServiceCommunicationException e) {
            log.error("Failed to communicate with external system for the webhook of assignment of the user with id {} in the domain with id {}", userId, domainId);
            throw new JobExecutionException("Failed communication with external system");
        }
    }
}

