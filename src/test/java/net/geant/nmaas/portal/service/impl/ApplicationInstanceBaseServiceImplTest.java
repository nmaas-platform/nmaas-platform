package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.applications.AppInstanceBase;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationInstanceBaseServiceImplTest {

    private final AppInstanceRepository appInstanceRepo = mock(AppInstanceRepository.class);
    private final ModelMapper modelMapper = mock(ModelMapper.class);
    private final ApplicationBaseService appBaseService = mock(ApplicationBaseService.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private final ApplicationInstanceService instanceService = mock(ApplicationInstanceService.class);

    private final ApplicationInstanceBaseServiceImpl service = new ApplicationInstanceBaseServiceImpl(
            appInstanceRepo,
            modelMapper,
            appBaseService,
            appDeploymentMonitor,
            instanceService
    );

    @Test
    void findAllByOwnerShouldFilterDeletedDomains() {
        User owner = new User("owner", true);
        owner.setId(10L);

        AppInstance activeInstance = appInstance(1L, 101L, false);
        AppInstance deletedDomainInstance = appInstance(2L, 102L, true);
        Pageable pageable = PageRequest.of(0, 20);

        when(appInstanceRepo.findAllByOwner(owner, pageable)).thenReturn(new PageImpl<>(List.of(activeInstance, deletedDomainInstance), pageable, 2));
        when(appBaseService.findByVersionId(any())).thenReturn(new ApplicationBase(555L, "base"));
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(any())).thenReturn(true);

        AppInstanceBase activeBase = new AppInstanceBase();
        activeBase.setDomainId(101L);
        when(modelMapper.map(activeInstance, AppInstanceBase.class)).thenReturn(activeBase);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, pageable);

        assertEquals(1, result.getContent().size());
        verify(modelMapper).map(eq(activeInstance), eq(AppInstanceBase.class));
        verify(modelMapper, never()).map(eq(deletedDomainInstance), eq(AppInstanceBase.class));
    }

    @Test
    void findAllWithFiltersShouldMapSortFieldsInPageable() {
        Pageable pageable = PageRequest.of(
                1,
                5,
                Sort.by(
                        Sort.Order.asc("owner"),
                        Sort.Order.desc("state"),
                        Sort.Order.asc("application"),
                        Sort.Order.desc("createdAt")
                )
        );
        when(appInstanceRepo.findAllNotDeletedByDeploy(any(), any(Pageable.class), eq(true)))
                .thenAnswer(inv -> {
                    Pageable mapped = inv.getArgument(1, Pageable.class);
                    List<String> properties = mapped.getSort().stream().map(Sort.Order::getProperty).toList();
                    assertEquals(List.of("owner.username", "l.state", "application.name", "createdAt"), properties);
                    return new PageImpl<AppInstance>(List.of(), mapped, 0);
                });

        service.findAll(pageable, true, "grafana");

        verify(appInstanceRepo).findAllNotDeletedByDeploy(eq("grafana"), any(Pageable.class), eq(true));
    }

    @Test
    void findAllShouldMapStateAndSkipUpgradeCheckForDoneState() {
        AppInstance instance = appInstance(3L, 200L, false);
        Pageable pageable = PageRequest.of(0, 10);

        when(appInstanceRepo.findAllNotDeleted(pageable)).thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(999L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(701L, "app-base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);

        Page<AppInstanceBase> result = service.findAll(pageable);

        assertEquals(AppInstanceState.DONE, result.getContent().getFirst().getState());
        assertEquals(200L, result.getContent().getFirst().getDomainId());
        assertEquals(701L, result.getContent().getFirst().getApplicationBaseId());
        verify(instanceService, never()).checkUpgradePossible(any());
    }

    @Test
    void findAllShouldSetUnknownStateWhenMonitorThrows() {
        AppInstance instance = appInstance(4L, 300L, false);
        Pageable pageable = PageRequest.of(0, 10);

        when(appInstanceRepo.findAllNotDeleted(pageable)).thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(300L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(801L, "app-base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenThrow(new RuntimeException("state lookup failed"));
        when(instanceService.checkUpgradePossible(4L)).thenReturn(false);

        Page<AppInstanceBase> result = service.findAll(pageable);

        assertEquals(AppInstanceState.UNKNOWN, result.getContent().getFirst().getState());
        verify(instanceService).checkUpgradePossible(4L);
    }

    @Test
    void findAllByOwnerShouldThrowWhenOwnerIdIsMissing() {
        User owner = new User("owner", true);
        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(IllegalArgumentException.class, () -> service.findAllByOwner(owner, pageable));
    }

    @Test
    void findAllByDomainShouldThrowWhenDomainIsNull() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(IllegalArgumentException.class, () -> service.findAllByDomain(null, pageable, true, "test"));
    }

    @Test
    void findAllByOwnerWithDeployAndSearchShouldUseRepositoryAndMap() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(5L, 501L, false);

        when(appInstanceRepo.findAllNotDeletedByOwnerAndByDeployAndSearch(owner, "graf", true, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(501L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(901L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(5L)).thenReturn(true);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, pageable, true, "graf");

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByOwnerAndByDeployAndSearch(owner, "graf", true, pageable);
    }

    @Test
    void findAllByOwnerAndDomainWithDeployShouldUseRepositoryWithNullSearch() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Domain domain = new Domain(20L, "d", "d");
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(6L, 20L, false);

        when(appInstanceRepo.findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(owner, null, domain, false, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(20L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(902L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS);
        when(instanceService.checkUpgradePossible(6L)).thenReturn(false);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, domain, pageable, false);

        assertEquals(AppInstanceState.UNDEPLOYING, result.getContent().getFirst().getState());
        verify(appInstanceRepo).findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(owner, null, domain, false, pageable);
    }

    @Test
    void findAllByOwnerWithSearchShouldUseRepositoryAndMap() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(8L, 801L, false);

        when(appInstanceRepo.findAllNotDeletedByOwnerAndSearch(owner, "prom", pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(801L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(904L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(8L)).thenReturn(false);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, pageable, "prom");

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByOwnerAndSearch(owner, "prom", pageable);
    }

    @Test
    void findAllByOwnerAndDomainWithSearchShouldUseRepository() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Domain domain = new Domain(40L, "d4", "d4");
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(9L, 40L, false);

        when(appInstanceRepo.findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(owner, "mon", domain, true, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(40L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(905L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(9L)).thenReturn(true);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, domain, pageable, true, "mon");

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(owner, "mon", domain, true, pageable);
    }

    @Test
    void findAllByOwnerAndDomainShouldUseOwnerSearchWithNullSearchValue() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Domain domain = new Domain(41L, "d41", "d41");
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(10L, 41L, false);

        when(appInstanceRepo.findAllNotDeletedByOwnerAndSearch(owner, null, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(41L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(906L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(10L)).thenReturn(true);

        Page<AppInstanceBase> result = service.findAllByOwner(owner, domain, pageable);

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByOwnerAndSearch(owner, null, pageable);
    }

    @Test
    void findAllByDomainWithSearchShouldPassNullSearchValue() {
        Domain domain = new Domain(30L, "d2", "d2");
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(7L, 30L, false);

        when(appInstanceRepo.findAllNotDeletedByDomainAndSearch(domain, null, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(30L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(903L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_CONFIGURATION_FAILED);
        when(instanceService.checkUpgradePossible(7L)).thenReturn(false);

        Page<AppInstanceBase> result = service.findAllByDomain(domain, pageable, "ignored");

        assertEquals(AppInstanceState.FAILURE, result.getContent().getFirst().getState());
        verify(appInstanceRepo).findAllNotDeletedByDomainAndSearch(domain, null, pageable);
    }

    @Test
    void findAllByDomainWithDeployShouldUseRepositoryWithNullSearch() {
        Domain domain = new Domain(31L, "d31", "d31");
        Pageable pageable = PageRequest.of(0, 5);
        AppInstance instance = appInstance(11L, 31L, false);

        when(appInstanceRepo.findAllNotDeletedByDomainAndByDeployAndSearch(domain, null, true, pageable))
                .thenReturn(new PageImpl<>(List.of(instance), pageable, 1));
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(31L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(907L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(11L)).thenReturn(true);

        Page<AppInstanceBase> result = service.findAllByDomain(domain, pageable, true);

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByDomainAndByDeployAndSearch(domain, null, true, pageable);
    }

    @Test
    void findAllByDomainWithDeployAndSearchShouldMapSortFields() {
        Domain domain = new Domain(32L, "d32", "d32");
        Pageable pageable = PageRequest.of(2, 3, Sort.by(Sort.Order.asc("owner"), Sort.Order.desc("application")));
        AppInstance instance = appInstance(12L, 32L, false);

        when(appInstanceRepo.findAllNotDeletedByDomainAndByDeployAndSearch(eq(domain), eq("api"), eq(false), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable mapped = inv.getArgument(3, Pageable.class);
                    List<String> properties = mapped.getSort().stream().map(Sort.Order::getProperty).toList();
                    assertEquals(List.of("owner.username", "application.name"), properties);
                    return new PageImpl<AppInstance>(List.of(instance), mapped, 1);
                });
        when(modelMapper.map(instance, AppInstanceBase.class)).thenReturn(mappedBaseWithDomainId(32L));
        when(appBaseService.findByVersionId(301L)).thenReturn(new ApplicationBase(908L, "base"));
        when(appDeploymentMonitor.state(instance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(instanceService.checkUpgradePossible(12L)).thenReturn(false);

        Page<AppInstanceBase> result = service.findAllByDomain(domain, pageable, false, "api");

        assertEquals(1, result.getContent().size());
        verify(appInstanceRepo).findAllNotDeletedByDomainAndByDeployAndSearch(eq(domain), eq("api"), eq(false), any(Pageable.class));
    }

    @Test
    void findAllByOwnerAndDomainShouldThrowWhenDomainIdIsMissing() {
        User owner = new User("owner", true);
        owner.setId(10L);
        Domain domain = new Domain(null, "no-id", "no-id");
        Pageable pageable = PageRequest.of(0, 5);

        assertThrows(IllegalArgumentException.class, () -> service.findAllByOwner(owner, domain, pageable, true));
    }

    @Test
    void mapAppInstanceStateShouldMapSelectedStates() {
        assertEquals(AppInstanceState.REQUESTED, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.REQUESTED));
        assertEquals(AppInstanceState.VALIDATION, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.REQUEST_VALIDATED));
        assertEquals(AppInstanceState.DEPLOYING, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS));
        assertEquals(AppInstanceState.REMOVED, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.FAILED_APPLICATION_REMOVED));
        assertEquals(AppInstanceState.UNKNOWN, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.UNKNOWN));
        assertEquals(AppInstanceState.PREPARATION, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS));
        assertEquals(AppInstanceState.CONNECTING, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS));
        assertEquals(AppInstanceState.CONFIGURATION_AWAITING, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
        assertEquals(AppInstanceState.RUNNING, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
        assertEquals(AppInstanceState.PAUSED, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.APPLICATION_PAUSED));
        assertEquals(AppInstanceState.DONE, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.APPLICATION_CONFIGURATION_REMOVED));
        assertEquals(AppInstanceState.FAILURE, ApplicationInstanceBaseServiceImpl.mapAppInstanceState(AppLifecycleState.APPLICATION_UPGRADE_FAILED));
    }

    private static AppInstance appInstance(Long id, Long domainId, boolean domainDeleted) {
        Domain domain = new Domain(domainId, "domain-" + domainId, "domain-" + domainId);
        domain.setDeleted(domainDeleted);

        Application application = new Application(301L, "app", "1.0.0");
        AppInstance appInstance = new AppInstance(application, domain, "instance-" + id, true);
        appInstance.setId(id);
        appInstance.setInternalId(new Identifier("deployment-" + id));
        return appInstance;
    }

    private static AppInstanceBase mappedBaseWithDomainId(Long domainId) {
        AppInstanceBase base = new AppInstanceBase();
        base.setDomainId(domainId);
        return base;
    }
}
