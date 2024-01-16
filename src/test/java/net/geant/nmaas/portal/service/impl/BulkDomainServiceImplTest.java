package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BulkDomainServiceImplTest {

    private final DomainService domainService = mock(DomainService.class);
    private final DomainGroupService domainGroupService = mock(DomainGroupService.class);
    private final UserService userService = mock(UserService.class);
    private final BulkDeploymentRepository bulkDeploymentRepository = mock(BulkDeploymentRepository.class);
    private final KubernetesClusterIngressManager kubernetesClusterIngressManager = mock(KubernetesClusterIngressManager.class);

    final BulkDomainService bulkDomainService = new BulkDomainServiceImpl(domainService, domainGroupService, userService,
            bulkDeploymentRepository, kubernetesClusterIngressManager, new ModelMapper(), 12);;

    @Test
    void shouldHandleBulkCreationWhenAllCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        Domain domain = new Domain(1L,"domain1", "domain1");
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.of(domain));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(Boolean.TRUE);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(1L);
        user.setEmail("user1@test.com");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.hasPrivilege((User) any(),any(),any())).thenReturn(true);
        when(bulkDeploymentRepository.save(any())).thenReturn(new BulkDeployment());

        bulkDomainService.handleBulkCreation(List.of(csvDomain), testUser());

        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository).save(bulkDeploymentArgumentCaptor.capture());
        BulkDeployment bulkDeployment = bulkDeploymentArgumentCaptor.getValue();
        assertEquals(BulkDeploymentState.COMPLETED, bulkDeployment.getState());
        assertEquals(BulkType.DOMAIN, bulkDeployment.getType());
        assertEquals(testUser().getId(), bulkDeployment.getCreatorId());
    }

    @Test
    void shouldHandleBulkCreationWhenDomainsCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        Domain domain = new Domain(1L,"domain1", "domain1");
        domain.setDomainDcnDetails(new DomainDcnDetails(10L, "domain1", true, DcnDeploymentType.MANUAL, null));
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.empty());
        when(domainService.createDomain(any())).thenReturn(domain);
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(Boolean.TRUE);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(1L);
        user.setEmail("user1@test.com");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.hasPrivilege((User) any(),any(),any())).thenReturn(true);
        when(bulkDeploymentRepository.save(any())).thenReturn(new BulkDeployment());

        bulkDomainService.handleBulkCreation(List.of(csvDomain), testUser());

        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository).save(bulkDeploymentArgumentCaptor.capture());
        BulkDeployment bulkDeployment = bulkDeploymentArgumentCaptor.getValue();
        assertEquals(BulkDeploymentState.COMPLETED, bulkDeployment.getState());
        assertEquals(BulkType.DOMAIN, bulkDeployment.getType());
        assertEquals(testUser().getId(), bulkDeployment.getCreatorId());
    }

    @Test
    void shouldHandleBulkCreationWhenUserCreated() {
        CsvDomain csvDomain = new CsvDomain("domain1", "user1", "user1@test.com", null, "group1");
        Domain domain = new Domain(1L,"domain1", "domain1");
        domain.setDomainDcnDetails(new DomainDcnDetails(10L, "domain1", true, DcnDeploymentType.MANUAL, null));
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        when(domainService.findDomain(anyString())).thenReturn(Optional.empty());
        when(domainService.createDomain(any())).thenReturn(domain);
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(domainGroupService.existDomainGroup("group1", "group1")).thenReturn(Boolean.TRUE);
        when(userService.existsByUsername("user1")).thenReturn(false);
        when(userService.existsByEmail("user1@test.com")).thenReturn(Boolean.FALSE);
        User user = new User("user1", true);
        user.setId(10L);
        user.setEmail("user1@test.com");
        when(userService.registerBulk(any(), any(), any())).thenReturn(user);
        when(bulkDeploymentRepository.save(any())).thenReturn(new BulkDeployment());

        bulkDomainService.handleBulkCreation(List.of(csvDomain), testUser());

        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository).save(bulkDeploymentArgumentCaptor.capture());
        BulkDeployment bulkDeployment = bulkDeploymentArgumentCaptor.getValue();
        assertEquals(BulkDeploymentState.COMPLETED, bulkDeployment.getState());
        assertEquals(BulkType.DOMAIN, bulkDeployment.getType());
        assertEquals(testUser().getId(), bulkDeployment.getCreatorId());
    }

    private static UserViewMinimal testUser() {
        UserViewMinimal testUser = new UserViewMinimal();
        testUser.setId(1L);
        testUser.setUsername("username");
        return testUser;
    }

}
