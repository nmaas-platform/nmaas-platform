package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainGroupServiceTest {

    private final DomainGroupRepository domainGroupRepository = mock(DomainGroupRepository.class);
    private final ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ModelMapper modelMapper = new ModelMapper();

    private DomainGroupService domainGroupService;

    @BeforeEach
    void setup() {
        domainGroupService = new DomainGroupServiceImpl(domainGroupRepository, applicationStatePerDomainService, eventPublisher, modelMapper);
    }

    @Test
    void shouldCreateDomainGroup() throws SchedulerException {
        // Setup domain group
        String name = "testgroup";
        String codename = "testgrp";
        DomainGroup domainGroup = new DomainGroup(name, codename);
        domainGroup.setId(10L);
        when(domainGroupRepository.save(any(DomainGroup.class))).thenReturn(domainGroup);
        when(domainGroupRepository.findById(10L)).thenReturn(Optional.of(domainGroup));

        // Create domain group
        DomainGroupView domainGroupView = new DomainGroupView();
        domainGroupView.setName(name);
        domainGroupView.setCodename(codename);
        DomainGroupView result = this.domainGroupService.createDomainGroup(domainGroupView);

        verify(eventPublisher, times(1)).publishEvent(any());

        // Verify domain group was created correctly
        assertThat("Codenames are not the same", result.getCodename().equals(codename));
        assertThat("Names are not the same", result.getName().equals(name));

        // Update domain group
        domainGroupView.setCodename(codename + "2");
        domainGroupView.setId(10L);
        result = this.domainGroupService.updateDomainGroup(10L, domainGroupView);

        verify(eventPublisher, times(1)).publishEvent(any());

        // Verify domain group was updated correctly
        assertThat("Updated codenames are not the same", result.getCodename().equals(codename + "2"));
        assertThat("Names are not the same after update", result.getName().equals(name));
    }

    @Test
    void shouldDeleteDomainGroup() throws SchedulerException {
        DomainGroup domainGroup = new DomainGroup("testgroup", "testgrp");
        domainGroup.setId(10L);
        when(domainGroupRepository.findById(10L)).thenReturn(Optional.of(domainGroup));

        domainGroupService.deleteDomainGroup(10L);

        verify(domainGroupRepository, times(1)).deleteById(10L);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

}
