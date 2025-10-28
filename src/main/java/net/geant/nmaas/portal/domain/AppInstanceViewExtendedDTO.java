package net.geant.nmaas.portal.domain;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;

import java.util.Objects;
import java.util.Set;

public record AppInstanceViewExtendedDTO(

        Long appId,
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
        String ownerUsername,
        String configuration,

        boolean autoUpgradesEnabled,
        boolean upgradePossible,
        boolean allowSshAccess,
        boolean configUpdateEnabled,
        boolean allowLogAccess,
        boolean configFileRepositoryRequired,

        AppConfigRepositoryAccessDetails appConfigRepositoryAccessDetails,
        ApplicationStatePerDomainView applicationStatePerDomain,
        ConfigWizardTemplateView configWizardTemplate,
        AppInstanceState state,
        Set<TagView> tags,
        Set<UserViewMinimal> members,
        AppInstanceView.AppInstanceUpgradeInfo upgradeInfo,
        Set<ServiceAccessMethodView> serviceAccessMethods
) {
    public AppInstanceViewExtendedDTO(AppInstanceViewExtended app) {

        this(
                app.getId(),
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
                app.getOwner().getUsername(),
                app.getConfiguration(),

                app.isAutoUpgradesEnabled(),
                app.isUpgradePossible(),
                app.getApplication().getApplication().getAppDeploymentSpec().isAllowSshAccess(),
                app.getApplication().getApplication().getAppConfigurationSpec().isConfigUpdateEnabled(),
                app.getApplication().getApplication().getAppDeploymentSpec().isAllowLogAccess(),
                app.getApplication().getApplication().getAppConfigurationSpec().isConfigFileRepositoryRequired(),

                app.getAppConfigRepositoryAccessDetails(),
                app.getDomain().getApplicationStatePerDomain().stream().filter(
                        state -> Objects.equals(
                                state.getApplicationBaseId(), app.getApplication().getApplicationBase().getId()
                        )
                ).findFirst().orElse(null),
                app.getConfigWizardTemplate(),
                app.getState(),
                app.getApplication().getApplicationBase().getTags(),
                app.getMembers(),
                app.getUpgradeInfo(),
                app.getServiceAccessMethods()

        );
    }
}


