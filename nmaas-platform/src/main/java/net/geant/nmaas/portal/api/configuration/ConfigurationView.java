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

    private String defaultLanguage;

    private boolean testInstance = false;

    public ConfigurationView(boolean maintenance, boolean ssoLoginAllowed, String defaultLanguage, boolean testInstance){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
    }
}
