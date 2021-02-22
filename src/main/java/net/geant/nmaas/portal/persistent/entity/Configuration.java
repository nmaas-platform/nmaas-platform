package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private boolean testInstance = false;

    @Column(nullable = false)
    private boolean sendAppInstanceFailureEmails = false;

    @Column(nullable = false)
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private String appInstanceFailureEmails = "";

    public Configuration(boolean maintenance, boolean ssoLoginAllowed, String defaultLanguage, boolean testInstance){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
    }

    public void setAppInstanceFailureEmailList(List<String> emails) {
        this.appInstanceFailureEmails = String.join(";", emails);
    }

    public List<String> getAppInstanceFailureEmailList() {
        return Arrays.asList(this.appInstanceFailureEmails.split(";"));
    }

    public Configuration(
            boolean maintenance,
            boolean ssoLoginAllowed,
            String defaultLanguage,
            boolean testInstance,
            boolean sendAppInstanceFailureEmails,
            List<String> appInstanceFailureEmailList
    ){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
        this.sendAppInstanceFailureEmails = sendAppInstanceFailureEmails;
        this.setAppInstanceFailureEmailList(appInstanceFailureEmailList);
    }

}
