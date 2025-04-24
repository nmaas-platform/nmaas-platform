package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.info.DashboardView;
import net.geant.nmaas.portal.api.info.DomainDashboardView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DashboardServiceImplTest {

    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DomainService domainService = mock(DomainService.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppInstanceRepository appInstanceRepo = mock(AppInstanceRepository.class);
    private final ApplicationBaseRepository applicationBaseRepository = mock(ApplicationBaseRepository.class);
    private final UserLoginRegisterService userLoginRegisterService = mock(UserLoginRegisterService.class);

    private final DashboardServiceImpl dashboardService = new DashboardServiceImpl(
            userService,
            userRepository,
            domainService,
            domainRepository,
            applicationInstanceService,
            appInstanceRepo,
            applicationBaseRepository,
            userLoginRegisterService
    );

    @Test
    void getSystemDashboardShouldThrowExceptionWhenRepositoryFails() {
        when(domainRepository.count()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, dashboardService::getSystemDashboard);
    }

    @Test
    void getSystemDomainDashboardShouldReturnEmptyObjectWhenDomainIdIsNull() {
        DomainDashboardView result = dashboardService.getSystemDomainDashboard(null);

        assert result != null;
        assert result.getUserLogins() == null;
        assert result.getApplicationDeployed() == null;
        assert result.getApplicationUpgradeStatus() == null;
    }

    @Test
    void getSystemDomainDashboardShouldHandleEmptyDomainUsers() {
        Long domainId = 1L;
        Domain domain = new Domain(domainId, "Test Domain", "test-domain");

        when(domainService.findDomain(domainId)).thenReturn(Optional.of(domain));
        when(domainService.getMembers(domainId)).thenReturn(Collections.emptyList());
        when(appInstanceRepo.findAllByDomain(domain)).thenReturn(Collections.emptyList());

        DomainDashboardView result = dashboardService.getSystemDomainDashboard(domainId);

        assert result != null;
        assert result.getUserLogins().isEmpty();
        assert result.getApplicationDeployed().isEmpty();
        assert result.getApplicationUpgradeStatus().isEmpty();
    }

    @Test
    void getSystemDomainDashboardShouldHandleEmptyAppInstances() {
        Long domainId = 1L;
        Domain domain = new Domain(domainId, "Test Domain", "test-domain");
        User user = new User("testUser", true);
        OffsetDateTime loginTime = OffsetDateTime.now();
        UserLoginRegister userLoginRegister = new UserLoginRegister(
                loginTime,
                user,
                UserLoginRegisterType.SUCCESS, // Typ logowania
                "127.0.0.1", // Adres IP
                "localhost", // Host
                "Mozilla/5.0" // User-Agent
        );

        when(domainService.findDomain(domainId)).thenReturn(Optional.of(domain));
        when(domainService.getMembers(domainId)).thenReturn(List.of(user));
        when(appInstanceRepo.findAllByDomain(domain)).thenReturn(Collections.emptyList());
        when(userLoginRegisterService.getLastLogin(user)).thenReturn(Optional.of(userLoginRegister));

        DomainDashboardView result = dashboardService.getSystemDomainDashboard(domainId);

        assert result != null;
        assert !result.getUserLogins().isEmpty();
        assert result.getApplicationUpgradeStatus().isEmpty();
    }

    @Test
    void getSystemDashboardShouldHandleEmptyBaseNames() {
        when(domainRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(10L);
        when(appInstanceRepo.count()).thenReturn(15L);
        when(appInstanceRepo.countAllDeployedSinceTime(anyLong())).thenReturn((int) 3L);
        when(applicationBaseRepository.findAllNames()).thenReturn(Collections.emptyList());

        DashboardView result = dashboardService.getSystemDashboard();

        assert result != null;
        assert result.getPopularApps().isEmpty();
    }
}
