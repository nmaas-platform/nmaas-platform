package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maintenance", nullable = false)
    private boolean maintenance = false;

    @Column(nullable = false)
    private boolean ssoLoginAllowed = false;

    @Column(nullable = false)
    private String defaultLanguage;

    public Configuration(boolean maintenance, boolean ssoLoginAllowed, String defaultLanguage){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
    }

}
