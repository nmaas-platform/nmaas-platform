package net.geant.nmaas.portal.api.domain;

import java.util.Set;

public record AppInstanceViewExtendedDTO(
        ApplicationDTO application,
        String applicationName,
        String applicationVersion,
        boolean autoUpgradesEnabled,
        ConfigWizardTemplateView configWizardTemplate,
        String descriptiveDeploymentId,
        String name,
        AppInstanceState state,
        boolean upgradePossible,
        boolean allowSshAccess,
        Long applicationBaseId
) {
    public AppInstanceViewExtendedDTO(AppInstanceViewExtended appInstanceViewExtended) {

        this(
                new ApplicationDTO(appInstanceViewExtended.getApplication()),
                appInstanceViewExtended.getApplicationName(),
                appInstanceViewExtended.getApplicationVersion(),
                appInstanceViewExtended.isAutoUpgradesEnabled(),
                appInstanceViewExtended.getConfigWizardTemplate(),
                appInstanceViewExtended.getDescriptiveDeploymentId(),
                appInstanceViewExtended.getName(),
                appInstanceViewExtended.getState(),
                appInstanceViewExtended.isUpgradePossible(),
                appInstanceViewExtended.getApplication().getApplication().getAppDeploymentSpec().isAllowSshAccess(),
                appInstanceViewExtended.getApplication().getApplicationBase().id

        );


    }

    private record ApplicationDTO(
            ApplicationBaseViewDTO applicationBase,
            ApplicationViewDTO application
    ) {
        public ApplicationDTO(net.geant.nmaas.portal.api.domain.ApplicationDTO application) {
            this(
                    new ApplicationBaseViewDTO(application.getApplicationBase()),
                    new ApplicationViewDTO(application.getApplication())
            );
        }
    }

    private record ApplicationBaseViewDTO(
            Long id,
            String name,
            String license,
            String licenseUrl,
            String wwwUrl,
            String sourceUrl,
            String issuesUrl,
            String nmaasDocumentationUrl,
            Set<TagView> tags
    ) {
        public ApplicationBaseViewDTO(ApplicationBaseView applicationBaseView) {
            this(
                    applicationBaseView.id,
                    applicationBaseView.getName(),
                    applicationBaseView.getLicense(),
                    applicationBaseView.getLicenseUrl(),
                    applicationBaseView.getWwwUrl(),
                    applicationBaseView.getSourceUrl(),
                    applicationBaseView.getIssuesUrl(),
                    applicationBaseView.getNmaasDocumentationUrl(),
                    applicationBaseView.getTags()
            );
        }
    }


    private record ApplicationViewDTO(
            boolean allowSshAccess,
            boolean configUpdateEnabled,
            boolean allowLogAccess
    ) {
        public ApplicationViewDTO(ApplicationView applicationView) {
            this(
                    applicationView.getAppDeploymentSpec().isAllowSshAccess(),
                    applicationView.getAppConfigurationSpec().isConfigUpdateEnabled(),
                    applicationView.getAppDeploymentSpec().isAllowLogAccess()
            );
        }
    }
}
