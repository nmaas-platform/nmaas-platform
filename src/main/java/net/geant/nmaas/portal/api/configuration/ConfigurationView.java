package net.geant.nmaas.portal.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

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

    private boolean sendAppInstanceFailureEmails = false;

    private List<@Email String> appInstanceFailureEmailList = new ArrayList<>();

    public ConfigurationView(boolean maintenance, boolean ssoLoginAllowed, String defaultLanguage, boolean testInstance, boolean sendAppInstanceFailureEmails, List<String> appInstanceFailureEmailList) {
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
        this.sendAppInstanceFailureEmails = sendAppInstanceFailureEmails;
        this.appInstanceFailureEmailList = appInstanceFailureEmailList;
    }
}
