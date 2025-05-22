package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.orchestration.jobs.DomainGroupJob;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.WebhookEvent;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.persistent.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistent.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainGroupServiceTest {

    DomainGroupRepository domainGroupRepository = mock(DomainGroupRepository.class);
    ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);
    WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
    Scheduler scheduler = mock(Scheduler.class);
    ListenerManager listenerManager = mock(ListenerManager.class);
    ScheduleManager scheduleManager;
    ModelMapper modelMapper = new ModelMapper();
    DomainGroupService domainGroupService;

    @BeforeEach
    void setup() {
        scheduleManager = new ScheduleManager(scheduler);
        domainGroupService = new DomainGroupServiceImpl(domainGroupRepository, applicationStatePerDomainService, webhookEventRepository, scheduleManager, modelMapper);
    }

    @Test
    void shouldCreateDomainGroup() throws SchedulerException {
        // Setup webhook event
        WebhookEvent webhookEvent = new WebhookEvent(1L, "webhook", "https://example.com/webhook", WebhookEventType.DOMAIN_GROUP_CHANGE, null, null);
        when(webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_GROUP_CHANGE))
                .thenReturn(Stream.of(1L));
        when(webhookEventRepository.findById(1L))
                .thenReturn(Optional.of(webhookEvent));

        // Setup domain group
        String name = "testgroup";
        String codename = "testgrp";
        DomainGroup domainGroup = new DomainGroup(name, codename);
        domainGroup.setId(10L);
        when(domainGroupRepository.save(any(DomainGroup.class))).thenReturn(domainGroup);
        when(domainGroupRepository.findById(10L)).thenReturn(Optional.of(domainGroup));
        when(scheduler.getListenerManager()).thenReturn(listenerManager);
        doNothing().when(listenerManager).addJobListener(any(JobListener.class), any(Matcher.class));

        // Create domain group
        DomainGroupView domainGroupView = new DomainGroupView();
        domainGroupView.setName(name);
        domainGroupView.setCodename(codename);
        DomainGroupView result = this.domainGroupService.createDomainGroup(domainGroupView);

        // Verify webhook job was scheduled with correct parameters for creation
        verify(scheduler, times(1)).scheduleJob(
            argThat(jobDetail -> 
                jobDetail.getKey().getName().startsWith("DomainGroup_1_10_") &&
                jobDetail.getJobClass().equals(DomainGroupJob.class)
            ),
            argThat(trigger -> 
                trigger.getKey().getName().startsWith("DomainGroup_1_10_")
            )
        );

        // Verify domain group was created correctly
        assertThat("Codenames are not the same", result.getCodename().equals(codename));
        assertThat("Names are not the same", result.getName().equals(name));

        // Update domain group
        when(webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_GROUP_CHANGE))
                .thenReturn(Stream.of(1L));
        when(webhookEventRepository.findById(1L))
                .thenReturn(Optional.of(webhookEvent));
        domainGroupView.setCodename(codename + "2");
        domainGroupView.setId(10L);
        result = this.domainGroupService.updateDomainGroup(10L, domainGroupView);

        // Verify webhook job was scheduled with correct parameters for update
        verify(scheduler, times(2)).scheduleJob(
            argThat(jobDetail -> 
                jobDetail.getKey().getName().startsWith("DomainGroup_1_10_") &&
                jobDetail.getJobClass().equals(DomainGroupJob.class)
            ),
            argThat(trigger -> 
                trigger.getKey().getName().startsWith("DomainGroup_1_10_")
            )
        );

        // Verify domain group was updated correctly
        assertThat("Updated codenames are not the same", result.getCodename().equals(codename + "2"));
        assertThat("Names are not the same after update", result.getName().equals(name));
    }

    @Test
    void shouldDeleteDomainGroup() throws SchedulerException {
        // Setup webhook event
        WebhookEvent webhookEvent = new WebhookEvent(1L, "webhook", "https://example.com/webhook", WebhookEventType.DOMAIN_GROUP_CHANGE, null, null);
        when(webhookEventRepository.findIdByEventType(WebhookEventType.DOMAIN_GROUP_CHANGE))
                .thenReturn(Stream.of(1L));
        when(webhookEventRepository.findById(1L))
                .thenReturn(Optional.of(webhookEvent));

        DomainGroup domainGroup = new DomainGroup("testgroup", "testgrp");
        domainGroup.setId(10L);
        when(domainGroupRepository.findById(10L)).thenReturn(Optional.of(domainGroup));
        when(scheduler.getListenerManager()).thenReturn(listenerManager);
        doNothing().when(listenerManager).addJobListener(any(JobListener.class), any(Matcher.class));
        this.domainGroupService.deleteDomainGroup(10L);
        verify(domainGroupRepository, times(1)).deleteById(10L);

        verify(scheduler, times(1)).scheduleJob(
                argThat(jobDetail ->
                        jobDetail.getKey().getName().startsWith("DomainGroup_1_10_") &&
                                jobDetail.getJobClass().equals(DomainGroupJob.class)
                ),
                argThat(trigger ->
                        trigger.getKey().getName().startsWith("DomainGroup_1_10_")
                )
        );
    }
}
