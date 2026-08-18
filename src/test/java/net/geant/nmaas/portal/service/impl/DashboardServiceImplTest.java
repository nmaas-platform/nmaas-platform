package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.DashboardDto;
import net.geant.nmaas.api.dto.DomainDashboardDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final DomainService domainService = mock(DomainService.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppInstanceRepository appInstanceRepository = mock(AppInstanceRepository.class);
    private final ApplicationBaseRepository applicationBaseRepository = mock(ApplicationBaseRepository.class);
    private final UserLoginRegisterService userLoginRegisterService = mock(UserLoginRegisterService.class);
    private final ApplicationBaseService appBaseService = mock(ApplicationBaseService.class);
    private final DomainGroupService domainGroupService = mock(DomainGroupService.class);

    private final DashboardServiceImpl dashboardService = new DashboardServiceImpl(
            userRepository,
            domainService,
            domainRepository,
            applicationInstanceService,
            appInstanceRepository,
            applicationBaseRepository,
            userLoginRegisterService,
            appBaseService,
            domainGroupService
    );

    @Test
    void getSystemDashboardShouldThrowExceptionWhenRepositoryFails() {
        when(domainRepository.count()).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () ->
                dashboardService.getSystemDashboard(OffsetDateTime.now().minusDays(1), OffsetDateTime.now())
        );
    }

    @Test
    void getDomainDashboardShouldReturnEmptyObjectWhenDomainIdIsNull() {
        DomainDashboardDto result = dashboardService.getDomainDashboard(null);

        assertNotNull(result);
        assertNull(result.getUserLogins());
        assertNull(result.getApplicationDeployed());
        assertNull(result.getApplicationUpgradeStatus());
    }

    @Test
    void getDomainDashboardShouldHandleEmptyDomainUsers() {
        Long domainId = 1L;
        Domain domain = new Domain(domainId, "Test Domain", "test-domain");

        when(domainService.findDomain(domainId)).thenReturn(Optional.of(domain));
        when(domainService.getMembers(domainId)).thenReturn(Collections.emptyList());
        when(appInstanceRepository.findAllByDomain(domain)).thenReturn(Collections.emptyList());

        DomainDashboardDto result = dashboardService.getDomainDashboard(domainId);

        assertNotNull(result);
        assertTrue(result.getUserLogins().isEmpty());
        assertTrue(result.getApplicationDeployed().isEmpty());
        assertTrue(result.getApplicationUpgradeStatus().isEmpty());
    }

    @Test
    void getDomainDashboardShouldHandleEmptyAppInstances() {
        Long domainId = 1L;
        Domain domain = new Domain(domainId, "Test Domain", "test-domain");
        User user = new User("testUser", true);
        OffsetDateTime loginTime = OffsetDateTime.now();
        UserLoginRegister userLoginRegister = new UserLoginRegister(
                loginTime,
                user,
                UserLoginRegisterType.SUCCESS, // login type
                "127.0.0.1", // IP address
                "localhost", // Host
                "Mozilla/5.0" // User-Agent
        );

        when(domainService.findDomain(domainId)).thenReturn(Optional.of(domain));
        when(domainService.getMembers(domainId)).thenReturn(List.of(user));
        when(appInstanceRepository.findAllByDomain(domain)).thenReturn(Collections.emptyList());
        when(userLoginRegisterService.getLastLogin(user)).thenReturn(Optional.of(userLoginRegister));

        DomainDashboardDto result = dashboardService.getDomainDashboard(domainId);

        assertNotNull(result);
        assertFalse(result.getUserLogins().isEmpty());
        assertTrue(result.getApplicationUpgradeStatus().isEmpty());
    }

    @Test
    void getSystemDashboardShouldHandleEmptyBaseNames() {
        when(domainRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(10L);
        when(appInstanceRepository.count()).thenReturn(15L);
        when(appInstanceRepository.countAllDeployedInTimePeriod(anyLong(), anyLong())).thenReturn((int) 3L);
        when(applicationBaseRepository.findAllNames()).thenReturn(Collections.emptyList());

        DashboardDto result = dashboardService.getSystemDashboard(OffsetDateTime.now().minusDays(1), OffsetDateTime.now());

        assertNotNull(result);
        assertTrue(result.getPopularApps().isEmpty());
    }

    @Test
    void getSystemDashboardShouldCalculateCorrectTimestamps() {
        OffsetDateTime startDate = OffsetDateTime.now().minusHours(5);
        OffsetDateTime endDate = OffsetDateTime.now();

        // Mock required repository methods
        when(domainRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(appInstanceRepository.count()).thenReturn(1L);
        when(appInstanceRepository.countAllDeployedInTimePeriod(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(1);
        when(applicationBaseRepository.findAllNames()).thenReturn(Collections.emptyList());
        when(appInstanceRepository.findAllInTimePeriod(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(Collections.emptyList());

        // Call the method
        dashboardService.getSystemDashboard(startDate, endDate);

        // Capture the arguments
        ArgumentCaptor<Long> startCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> endCaptor = ArgumentCaptor.forClass(Long.class);

        verify(appInstanceRepository).countAllDeployedInTimePeriod(startCaptor.capture(), endCaptor.capture());

        long startTimestamp = startCaptor.getValue();
        long endTimestamp = endCaptor.getValue();

        // The timestamps should be positive and start should be less than or equal to end
        assertTrue(startTimestamp > 0);
        assertTrue(endTimestamp > 0);
        assertTrue(startTimestamp <= endTimestamp);
    }

    @Test
    void countAllDeployedSinceTimeShouldReturnCorrectCount() {
        long sinceTime = 1000L;
        long toTime = 2000L;
        int expectedCount = 7;

        when(appInstanceRepository.countAllDeployedInTimePeriod(sinceTime, toTime)).thenReturn(expectedCount);

        int actualCount = appInstanceRepository.countAllDeployedInTimePeriod(sinceTime, toTime);

        assertEquals(expectedCount, actualCount);
        verify(appInstanceRepository).countAllDeployedInTimePeriod(sinceTime, toTime);
    }

    @Test
    void getSystemDashboardShouldCallCountAllDeployedSinceTimeWithCorrectArguments() {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(2);
        OffsetDateTime endDate = OffsetDateTime.now();

        when(domainRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(appInstanceRepository.count()).thenReturn(1L);
        when(appInstanceRepository.countAllDeployedInTimePeriod(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(5);
        when(applicationBaseRepository.findAllNames()).thenReturn(Collections.emptyList());
        when(appInstanceRepository.findAllInTimePeriod(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(Collections.emptyList());

        dashboardService.getSystemDashboard(startDate, endDate);

        ArgumentCaptor<Long> sinceCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> toCaptor = ArgumentCaptor.forClass(Long.class);

        verify(appInstanceRepository).countAllDeployedInTimePeriod(sinceCaptor.capture(), toCaptor.capture());

        long sinceTime = sinceCaptor.getValue();
        long toTime = toCaptor.getValue();

        assertTrue(sinceTime > 0);
        assertTrue(toTime > 0);
        assertTrue(sinceTime < toTime);
    }

    @Test
    void getOperatorDashboardShouldReturnCorrectDomainCount() {
        // Mock the domain repository to return a specific count
        long expectedDomainCount = 10L;
        when(domainRepository.countByActiveTrueAndDeletedFalse()).thenReturn(expectedDomainCount);

        // Call the method
        DashboardDto result = dashboardService.getOperatorDashboard();

        // Verify the result
        assertNotNull(result);
        assertEquals(expectedDomainCount, (long) result.getDomainsCount());

        // Verify that the repository method was called
        verify(domainRepository).countByActiveTrueAndDeletedFalse();
    }
}
