package net.geant.nmaas.externalservices.inventory.shibboleth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShibbolethView {

    private Long id;
    private String loginUrl;
    private String logoutUrl;
    private int timeout;

}
