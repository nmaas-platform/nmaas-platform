package net.geant.nmaas.portal.api.domain;

import java.util.List;
import java.util.Set;

public record AppInstanceViewExtendedDTO(

        Long appBaseId,
        Long domainId,
        String appBaseName,
        String name,
        String appLicense,
        String appLicenseUrl,
        String appWwwUrl,
        String appSourceUrl,
        String appIssuesUrl,
        String appNmaasDocumentationUrl,
        String applicationName,
        String applicationVersion,
        String descriptiveDeploymentId,
        String chartVersion,

        boolean autoUpgradesEnabled,
        boolean upgradePossible,
        boolean allowSshAccess,
        boolean configUpdateEnabled,
        boolean allowLogAccess,
        boolean configFileRepositoryRequired,

        ConfigWizardTemplateView configWizardTemplate,
        AppInstanceState state,
        Set<TagView> tags,
        List<ApplicationStatePerDomainView>applicationStatePerDomain
) {
    public AppInstanceViewExtendedDTO(AppInstanceViewExtended app) {

        this(
                app.getApplication().getApplicationBase().getId(),
                app.getDomain().getId(),
                app.getApplication().getApplicationBase().getName(),
                app.getName(),
                app.getApplication().getApplicationBase().getLicense(),
                app.getApplication().getApplicationBase().getLicenseUrl(),
                app.getApplication().getApplicationBase().getWwwUrl(),
                app.getApplication().getApplicationBase().getSourceUrl(),
                app.getApplication().getApplicationBase().getIssuesUrl(),
                app.getApplication().getApplicationBase().getNmaasDocumentationUrl(),
                app.getApplication().getApplication().getName(),
                app.getApplication().getApplication().getVersion(),
                app.getDescriptiveDeploymentId(),
                app.getApplication().getApplication().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion(),

                app.isAutoUpgradesEnabled(),
                app.isUpgradePossible(),
                app.getApplication().getApplication().getAppDeploymentSpec().isAllowSshAccess(),
                app.getApplication().getApplication().getAppConfigurationSpec().isConfigUpdateEnabled(),
                app.getApplication().getApplication().getAppDeploymentSpec().isAllowLogAccess(),
                app.getApplication().getApplication().getAppConfigurationSpec().isConfigFileRepositoryRequired(),

                app.getConfigWizardTemplate(),
                app.getState(),
                app.getApplication().getApplicationBase().getTags(),
                app.getDomain().getApplicationStatePerDomain()

        );
    }
}


