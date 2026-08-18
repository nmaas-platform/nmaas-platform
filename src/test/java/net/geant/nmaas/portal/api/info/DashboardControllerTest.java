package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.dashboard.DashboardController;
import net.geant.nmaas.api.dto.DashboardDto;
import net.geant.nmaas.api.dto.DomainDashboardDto;
import net.geant.nmaas.portal.service.DashboardService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    private final DashboardService dashboardService = mock(DashboardService.class);
    private final DashboardController dashboardController = new DashboardController(dashboardService);

    @Test
    void shouldGetDashboardAdmin() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now();
        DashboardDto dashboardDto = DashboardDto.builder().domainsCount(1L).build();

        when(dashboardService.getSystemDashboard(start, end)).thenReturn(dashboardDto);

        DashboardDto result = dashboardController.getDashboardAdmin(start, end);

        assertNotNull(result);
        assertEquals(1L, result.getDomainsCount());
    }

    @Test
    void shouldGetDashboardDomain() {
        Long domainId = 1L;
        DomainDashboardDto domainDashboardDto = DomainDashboardDto.builder().build();

        when(dashboardService.getDomainDashboard(domainId)).thenReturn(domainDashboardDto);

        DomainDashboardDto result = dashboardController.getDashboardDomain(domainId);

        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsNull() {
        OffsetDateTime end = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardController.getDashboardAdmin(null, end);
        });
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsNull() {
        OffsetDateTime start = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardController.getDashboardAdmin(start, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusDays(1);
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardController.getDashboardAdmin(start, end);
        });
    }
}
