package net.geant.nmaas.portal.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConfigurationView {

    private Long id;

    private boolean maintenance = false;

    private boolean ssoLoginAllowed = false;

    public ConfigurationView(boolean maintenance, boolean ssoLoginAllowed){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
    }
}
