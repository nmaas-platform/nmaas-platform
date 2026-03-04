package net.geant.nmaas.portal.api.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;

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

    @Builder.Default
    private boolean registrationDomainSelectionEnabled = true;

    @Builder.Default
    private boolean bulkDomainsAllowForSsoAccounts = true;

    @Builder.Default
    private boolean bulkDomainsSendEmailForNewAccounts = true;

    private String bulkDeploymentJobCron;

    private Integer parallelDeploymentsLimit;

    private Integer bulkDeploymentQueueRefresh;

    private Integer bulkDeploymentTimeThreshold;

    private String deploymentPrefix;

    private String healthCheckJobCron;

    private Long defaultDomainForSsoUsers;

    private Integer appInstanceNameLengthLimit;

}