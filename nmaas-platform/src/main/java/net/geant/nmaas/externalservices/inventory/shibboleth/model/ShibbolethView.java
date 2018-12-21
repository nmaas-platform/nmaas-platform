package net.geant.nmaas.externalservices.inventory.shibboleth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShibbolethView {
    private String loginUrl;
    private String logoutUrl;
}
