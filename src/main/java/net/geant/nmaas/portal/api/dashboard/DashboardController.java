package net.geant.nmaas.portal.api.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Retrieving dashboard data")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public DashboardDto getDashboardAdmin(@RequestParam("startDate") OffsetDateTime startDate,
                                          @RequestParam("end") OffsetDateTime endDate) {
        validateRequestedPeriod(startDate, endDate);
        return dashboardService.getSystemDashboard(startDate, endDate);
    }

    @GetMapping("/operator")
    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    public DashboardDto getDashboardOperator() {
        return dashboardService.getOperatorDashboard();
    }

    @GetMapping("/domain/{id}")
    public DomainDashboardDto getDashboardDomain(@PathVariable Long id) {
        return dashboardService.getDomainDashboard(id);
    }
    @GetMapping("/group/{id}")
    public DomainGroupDashboardDto getDashboardGroup(@PathVariable Long id) {
        return dashboardService.getDomainGroupDashboard(id);
    }

    private void validateRequestedPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date can not be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date can not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date is after end date.");
        }
    }
}
