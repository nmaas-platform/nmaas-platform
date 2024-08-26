package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "maintenance", nullable = false)
    private boolean maintenance = false;

    @Builder.Default

    @Column(nullable = false)
    private boolean ssoLoginAllowed = false;

    @Column(nullable = false)
    private String defaultLanguage;

    @Builder.Default
    @Column(nullable = false)
    private boolean testInstance = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean sendAppInstanceFailureEmails = false;

    @Column(nullable = false)
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private String appInstanceFailureEmails = "";

    @Builder.Default
    @Column(nullable = false)
    private boolean registrationDomainSelectionEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean bulkDomainsAllowForSsoAccounts = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean bulkDomainsSendEmailForNewAccounts = true;

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
            List<String> appInstanceFailureEmailList,
            boolean registrationDomainSelectionEnabled,
            boolean bulkDomainsAllowForSsoAccounts,
            boolean bulkDomainsSendEmailForNewAccounts
    ){
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
        this.sendAppInstanceFailureEmails = sendAppInstanceFailureEmails;
        this.setAppInstanceFailureEmailList(appInstanceFailureEmailList);
        this.registrationDomainSelectionEnabled = registrationDomainSelectionEnabled;
        this.bulkDomainsAllowForSsoAccounts = bulkDomainsAllowForSsoAccounts;
        this.bulkDomainsSendEmailForNewAccounts = bulkDomainsSendEmailForNewAccounts;
    }

}
