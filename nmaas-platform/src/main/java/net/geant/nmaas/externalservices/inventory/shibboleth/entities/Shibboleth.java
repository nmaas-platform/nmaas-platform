package net.geant.nmaas.externalservices.inventory.shibboleth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shibboleth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String loginUrl;

    @Column(nullable = false)
    private String logoutUrl;

    @Column(nullable = false)
    private boolean allowsBasic = true;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private int timeout;

    public Shibboleth(String loginUrl, String logoutUrl, boolean allowsBasic, String key, int timeout){
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
        this.allowsBasic = allowsBasic;
        this.key = key;
        this.timeout = timeout;
    }

}
