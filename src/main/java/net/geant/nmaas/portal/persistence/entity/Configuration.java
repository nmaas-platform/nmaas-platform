package net.geant.nmaas.portal.persistence.entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

import java.util.ArrayList;
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

    @Builder.Default
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

    @Column(name = "bulk_deployment_job_cron", nullable = false)
    private String bulkDeploymentJobCron;

    @Column(name = "parallel_deployments_limit", nullable = false)
    private Integer parallelDeploymentsLimit;

    @Column(name = "bulk_deployment_queue_refresh_seconds", nullable = false)
    private Integer bulkDeploymentQueueRefresh;

    @Column(name = "parallel_deployments_time_threshold", nullable = false)
    private Integer bulkDeploymentTimeThreshold;

    @Column(nullable = true, length = 5)
    private String deploymentPrefix;

    @Column(name = "health_check_job_cron", nullable = false)
    private String healthCheckJobCron;

    @ManyToOne
    @JoinColumn(name = "default_domain_for_sso_users")
    private Domain defaultDomainForSsoUsers;

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
        this.bulkDeploymentQueueRefresh = 60;
    }

    public void setAppInstanceFailureEmailList(List<String> emails) {
        this.appInstanceFailureEmails = String.join(";", emails);
    }

    public List<String> getAppInstanceFailureEmailList() {
        return Arrays.asList(this.appInstanceFailureEmails.split(";"));
    }

}
