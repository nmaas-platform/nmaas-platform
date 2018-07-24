package net.geant.nmaas.portal.api.maintenance;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@ApplicationScope
@Component
public class MaintenanceManager {

    private Maintenance maintenance = new Maintenance();

    public boolean isMaintenance() {
        return maintenance.isMaintenance();
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    public void setMaintenance(boolean flag){
        this.maintenance.setMaintenance(flag);
    }
}
