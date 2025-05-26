package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@AllArgsConstructor
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    private DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public DashboardView getDashboardAdmin( @RequestParam("startDate") OffsetDateTime startDate,
                                            @RequestParam("end") OffsetDateTime endDate) {
        checkDate(startDate,endDate);
        DashboardView view = dashboardService.getSystemDashboard(startDate,endDate);
        log.error("View : {}", view.toString());

        return view;
    }

    @GetMapping("/domain/{id}")
    public DomainDashboardView getDashboardDomain(@PathVariable Long id) {
        DomainDashboardView view = dashboardService.getSystemDomainDashboard(id);
        log.error("View : {}", view.toString());
        return view;
    }

    private void checkDate(OffsetDateTime startDate, OffsetDateTime endDate) {
        if(startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date can not be null");
        }

        if(startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date is after end date.");
        }
    }
}
