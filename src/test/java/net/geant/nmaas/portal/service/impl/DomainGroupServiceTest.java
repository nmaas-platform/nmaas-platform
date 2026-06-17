package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserInfoDto;
import net.geant.nmaas.api.dto.users.UserRoleDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private final UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);

    private DomainGroupService domainGroupService;

    @BeforeEach
    void setup() {
        domainGroupService = new DomainGroupServiceImpl(domainGroupRepository, applicationStatePerDomainService, eventPublisher, userRoleRepository, modelMapper);
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
        DomainGroupDto domainGroupView = new DomainGroupDto();
        domainGroupView.setName(name);
        domainGroupView.setCodename(codename);
        DomainGroupDto result = this.domainGroupService.createDomainGroup(domainGroupView);

        verify(eventPublisher, times(1)).publishEvent(any());

        // Verify domain group was created correctly
        assertThat("Codenames are not the same", result.getCodename().equals(codename));
        assertThat("Names are not the same", result.getName().equals(name));

        // Update domain group
        domainGroupView.setCodename(codename + "2");
        domainGroupView.setId(10L);
        result = this.domainGroupService.updateDomainGroup(10L, domainGroupView);

        verify(eventPublisher, times(2)).publishEvent(any());

        // Verify domain group was updated correctly
        assertThat("Updated codenames are not the same", result.getCodename().equals(codename + "2"));
        assertThat("Names are not the same after update", result.getName().equals(name));
    }

    @Test
    void shouldNotStoreSystemAdminsAsDomainGroupManagers() {
        DomainGroup savedDomainGroup = new DomainGroup("testgroup", "testgrp");
        savedDomainGroup.setId(10L);
        ArgumentCaptor<DomainGroup> domainGroupCaptor = ArgumentCaptor.forClass(DomainGroup.class);
        when(domainGroupRepository.save(domainGroupCaptor.capture())).thenReturn(savedDomainGroup);

        DomainGroupDto domainGroupView = new DomainGroupDto();
        domainGroupView.setName("testgroup");
        domainGroupView.setCodename("testgrp");
        domainGroupView.setManagers(List.of(
                userView(1L, "system-admin", RoleDto.ROLE_SYSTEM_ADMIN),
                userView(2L, "group-manager", RoleDto.ROLE_GROUP_MANAGER)
        ));

        domainGroupService.createDomainGroup(domainGroupView);

        DomainGroup persistedDomainGroup = domainGroupCaptor.getValue();
        assertEquals(1, persistedDomainGroup.getManagers().size());
        assertEquals("group-manager", persistedDomainGroup.getManagers().getFirst().getUsername());
        assertFalse(persistedDomainGroup.getManagers().stream()
                .anyMatch(manager -> manager.getUsername().equals("system-admin")));
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

    @Test
    void shouldDeleteGroupDomainAdminRolesWhenDomainIsRemovedFromGroup() {
        Domain domain = new Domain(1L, "domain", "domain");
        DomainGroup domainGroup = new DomainGroup("testgroup", "testgrp");
        domainGroup.setId(10L);
        User manager1 = new User("manager1");
        manager1.setId(100L);
        User manager2 = new User("manager2");
        manager2.setId(101L);
        domainGroup.setManagers(List.of(manager1, manager2));
        domainGroup.addDomain(domain);
        when(domainGroupRepository.findById(10L)).thenReturn(Optional.of(domainGroup));
        when(domainGroupRepository.save(domainGroup)).thenReturn(domainGroup);

        domainGroupService.deleteDomainFromGroup(domain, 10L);

        verify(userRoleRepository, times(1)).deleteBy(100L, 1L, Role.ROLE_GROUP_DOMAIN_ADMIN);
        verify(userRoleRepository, times(1)).deleteBy(101L, 1L, Role.ROLE_GROUP_DOMAIN_ADMIN);
        verify(domainGroupRepository, times(1)).save(domainGroup);
        assertFalse(domainGroup.getDomains().contains(domain));
    }

    private static UserInfoDto userView(Long id, String username, RoleDto role) {
        UserInfoDto user = new UserInfoDto();
        user.setId(id);
        user.setUsername(username);
        user.setRoles(Set.of(new UserRoleDto(role, 1L, "domain")));
        return user;
    }

}
