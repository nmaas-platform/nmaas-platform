package net.geant.nmaas.portal.api.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Maintenance {
    public Maintenance(){}

    public Maintenance(boolean maintenance){
        this.maintenance = maintenance;
    }

    @JsonProperty
    private Boolean maintenance = true;

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}
