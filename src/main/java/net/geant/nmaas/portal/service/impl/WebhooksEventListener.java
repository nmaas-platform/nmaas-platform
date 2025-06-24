package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.jobs.DomainCreationJob;
import net.geant.nmaas.orchestration.jobs.DomainGroupJob;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.events.DomainCreatedEvent;
import net.geant.nmaas.portal.events.DomainGroupChangedEvent;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.persistent.repositories.WebhookEventRepository;
import net.geant.nmaas.scheduling.ScheduleManager;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
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

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional
    public void trigger(DomainCreatedEvent event) {
        final Domain domain = event.getDomainEntity();
        webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_CREATION)
                .forEach(id -> scheduleManager.createOneTimeJob(DomainCreationJob.class, "DomainCreation_" + id + "_" + domain.getId(), Map.of("webhookId", id, "domainId", domain.getId())));
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional
    public void trigger(DomainGroupChangedEvent event) {
        final DomainGroupView domainGroup = event.getDomainGroup();
        webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_GROUP_CHANGE)
                .forEach(id -> scheduleManager.createOneTimeJob(DomainGroupJob.class,"DomainGroup_" + id + "_" + domainGroup.getId() + "_" + LocalDateTime.now(), Map.of("webhookId", id, "action", event.getAction(), "domainGroup", domainGroup)));
    }

}