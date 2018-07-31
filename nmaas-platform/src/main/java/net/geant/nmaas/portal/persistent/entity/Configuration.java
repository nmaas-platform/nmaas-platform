package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.*;

@Entity
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maintenance", nullable = false)
    private Boolean maintenance = false;

    public Configuration(){}

    public Configuration(boolean maintenance){
        this.maintenance = maintenance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}
