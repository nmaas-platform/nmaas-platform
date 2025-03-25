package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    private DashboardService dashboardService;

    @GetMapping("/admin")
    public DashboardView getDashboardAdmin() {
        DashboardView view = dashboardService.getSystemDomainDashboard();
        log.error("View : {}", view.toString());

        return view;
    }

    @GetMapping("/domain")
    public void getDashboardDomain() {
        ;
    }
}
