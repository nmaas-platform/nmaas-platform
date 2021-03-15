package net.geant.nmaas.portal.api.configuration;

import lombok.*;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConfigurationView {

    private Long id;

    @Builder.Default
    private boolean maintenance = false;
    @Builder.Default
    private boolean ssoLoginAllowed = false;

    private String defaultLanguage;
    @Builder.Default
    private boolean testInstance = false;
    @Builder.Default
    private boolean sendAppInstanceFailureEmails = false;
    @Builder.Default
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
