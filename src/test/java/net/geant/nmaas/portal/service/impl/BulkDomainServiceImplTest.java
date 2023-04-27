package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkDomainServiceImplTest {

    DomainService domainService = mock(DomainService.class);
    DomainGroupService domainGroupService = mock(DomainGroupService.class);
    UserService userService = mock(UserService.class);

    BulkDomainService bulkDomainService;

    @BeforeEach
    void setup() {
        bulkDomainService = new BulkDomainServiceImpl(domainService, domainGroupService, userService);
    }

    @Test
    void shouldHandleBulkCreationWhenAllCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        List<CsvBean> input = List.of(new CsvDomain("domain1", "user1", "user1@test.com", null, "group1"));
        Domain domain = new Domain(1L,"domain1", "domain1");
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.of(domain));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        DomainGroup domainGroup = new DomainGroup("group1", "group1");
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(Boolean.TRUE);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(1L);
        user.setEmail("user1@test.com");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.hasPrivilege(any(),any(),any())).thenReturn(true);
        List<BulkDeploymentEntryView> responses = bulkDomainService.handleBulkCreation(input);
        assertEquals(responses.size(), 2);
        assertEquals(responses.get(0).getCreated(), false);
        assertEquals(responses.get(0).getSuccessful(), true);
        assertEquals(responses.get(0).getType(), BulkType.DOMAIN);
        assertEquals(responses.get(1).getCreated(), false);
        assertEquals(responses.get(1).getSuccessful(), true);
        assertEquals(responses.get(1).getType(), BulkType.USER);
    }

    @Test
    void shouldHandleBulkCreationWhenDomainCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        List<CsvBean> input = List.of(new CsvDomain("domain1", "user1", "user1@test.com", null, "group1"));
        Domain domain = new Domain(1L,"domain1", "domain1");
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.empty());
        when(domainService.createDomain(any())).thenReturn(domain);
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        DomainGroup domainGroup = new DomainGroup("group1", "group1");
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(Boolean.TRUE);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(1L);
        user.setEmail("user1@test.com");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.hasPrivilege(any(),any(),any())).thenReturn(true);
        List<BulkDeploymentEntryView> responses = bulkDomainService.handleBulkCreation(input);
        assertEquals(responses.size(), 2);
        assertEquals(responses.get(0).getCreated(), true);
        assertEquals(responses.get(0).getSuccessful(), true);
        assertEquals(responses.get(0).getType(), BulkType.DOMAIN);
        assertEquals(responses.get(1).getCreated(), false);
        assertEquals(responses.get(1).getSuccessful(), true);
        assertEquals(responses.get(1).getType(), BulkType.USER);
    }

    @Test
    void shouldHandleBulkCreationWhenUserCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        List<CsvBean> input = List.of(new CsvDomain("domain1", "user1", "user1@test.com", null, "group1"));
        Domain domain = new Domain(1L,"domain1", "domain1");
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.empty());
        when(domainService.createDomain(any())).thenReturn(domain);
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        DomainGroup domainGroup = new DomainGroup("group1", "group1");
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(false);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(10L);
        user.setEmail("user1@test.com");
        when(userService.registerBulk(any(), any(), any())).thenReturn(user);
        List<BulkDeploymentEntryView> responses = bulkDomainService.handleBulkCreation(input);
        assertEquals(responses.size(), 2);
        assertEquals(responses.get(0).getCreated(), true);
        assertEquals(responses.get(0).getSuccessful(), true);
        assertEquals(responses.get(0).getType(), BulkType.DOMAIN);
        assertEquals(responses.get(1).getCreated(), true);
        assertEquals(responses.get(1).getSuccessful(), true);
        assertEquals(responses.get(1).getType(), BulkType.USER);
        assertEquals(responses.get(1).getDetails().get("userName"), "user1");
        assertEquals(responses.get(1).getDetails().get("userId"), "10");
        assertEquals(responses.get(1).getDetails().get("email"), "user1@test.com");
    }
}
