package net.geant.nmaas.webhooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.webhooks.jobs.AppDeploymentJob;
import net.geant.nmaas.webhooks.jobs.AppRemovalJob;
import net.geant.nmaas.webhooks.jobs.DomainActionJob;
import net.geant.nmaas.webhooks.jobs.DomainGroupActionJob;
import net.geant.nmaas.webhooks.jobs.UserDomainAssignmentJob;
import net.geant.nmaas.portal.domain.DomainBase;
import net.geant.nmaas.portal.domain.DomainGroupView;
import net.geant.nmaas.portal.events.ApplicationDeployedEvent;
import net.geant.nmaas.portal.events.ApplicationRemovedEvent;
import net.geant.nmaas.portal.events.DomainCreatedEvent;
import net.geant.nmaas.portal.events.DomainGroupChangedEvent;
import net.geant.nmaas.portal.events.DomainRemovalEvent;
import net.geant.nmaas.portal.events.UserDomainAssignmentEvent;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.scheduling.ScheduleManager;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.modelmapper.ModelMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhooksEventListener {

    private final WebhookEventRepository webhookEventRepository;
    private final ScheduleManager scheduleManager;
    private final ModelMapper modelMapper;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional
    public void trigger(DomainCreatedEvent event) {
        final Domain domain = event.getDomainEntity();
        final DomainBase body = modelMapper.map(domain, DomainBase.class);
        webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_ACTION)
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                DomainActionJob.class,
                                "DomainCreate_" + id + "_" + domain.getId(),
                                Map.of("webhookId", id, "domain", body, "action", "create")
                        )
                );
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional
    public void trigger(DomainRemovalEvent event) {
        final DomainBase domain = DomainBase.fromView(event.getDomainView());
        String action = event.isHardRemoval() ? "delete" : "softDelete";
        webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_ACTION)
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                DomainActionJob.class,
                                "Domain" + action + "_" + id + "_" + domain.getId(),
                                Map.of("webhookId", id, "domain", domain, "action", action)
                        )
                );
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional
    public void trigger(DomainGroupChangedEvent event) {
        final DomainGroupView domainGroup = event.getDomainGroup();
        domainGroup.setManagers(null);
        domainGroup.setApplicationStatePerDomain(null);
        webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_GROUP_ACTION)
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                DomainGroupActionJob.class,
                                "DomainGroup_" + id + "_" + domainGroup.getId() + "_" + LocalDateTime.now(),
                                Map.of("webhookId", id, "action", event.getAction(), "domainGroup", domainGroup)
                        )
                );
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(UserDomainAssignmentEvent event) {
        webhookEventRepository.findIdByEventTypeAndDomain(WebhookEventType.USER_ASSIGNMENT, event.getDomainId())
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                UserDomainAssignmentJob.class,
                                "UserDomainAssignmentJobCreate_" + id + "_user" + event.getUserId() + "_domain" + event.getDomainId() + "_" + LocalDateTime.now(),
                                Map.of("webhookId", id, "domainId", event.getDomainId(), "userId", event.getUserId(), "role", event.getRole(), "action", event.getAction())
                        )
                );
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(ApplicationDeployedEvent event) {
        webhookEventRepository.findIdByEventTypeAndDeployment(WebhookEventType.APPLICATION_DEPLOYMENT, event.getDeploymentId())
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                AppDeploymentJob.class,
                                "AppDeploymentJob_" + id + "_" + event.getDeploymentId() + "_time" + LocalDateTime.now(),
                                Map.of("webhookId", id, "deploymentId", event.getDeploymentId())
                        )
                );
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(ApplicationRemovedEvent event) {
        webhookEventRepository.findIdByEventTypeAndDeployment(WebhookEventType.APPLICATION_REMOVAL, event.getDeploymentId())
                .forEach(id ->
                        scheduleManager.createOneTimeJob(
                                AppRemovalJob.class,
                                "AppRemovalJob_" + id + "_" + event.getDeploymentId() + "_time" + LocalDateTime.now(),
                                Map.of("webhookId", id, "deploymentId", event.getDeploymentId())
                        )
                );
    }

}