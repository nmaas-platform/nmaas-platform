package net.geant.nmaas.portal.api.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/maintenance")
public class MaintenanceController {

    private MaintenanceManager maintenanceManager;

    @Autowired
    public MaintenanceController(MaintenanceManager maintenanceManager) {
        this.maintenanceManager = maintenanceManager;
    }

    @GetMapping
    public Maintenance getMaintenance(){
        return new Maintenance(maintenanceManager.isMaintenance());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    public void updateMaintenance(@RequestBody Maintenance maintenance){
        this.maintenanceManager.setMaintenance(maintenance);
    }
}
