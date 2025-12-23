package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.persistence.entity.Configuration;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.modelmapper.AbstractConverter;

public class ConfigurationConverter extends AbstractConverter<Configuration, ConfigurationView> {

    @Override
    protected ConfigurationView convert(Configuration source) {
        return ConfigurationView.builder()
                .id(source.getId())
                .maintenance(source.isMaintenance())
                .ssoLoginAllowed(source.isSsoLoginAllowed())
                .defaultLanguage(source.getDefaultLanguage())
                .testInstance(source.isTestInstance())
                .sendAppInstanceFailureEmails(source.isSendAppInstanceFailureEmails())
                .appInstanceFailureEmailList(source.getAppInstanceFailureEmailList())
                .registrationDomainSelectionEnabled(source.isRegistrationDomainSelectionEnabled())
                .bulkDomainsAllowForSsoAccounts(source.isBulkDomainsAllowForSsoAccounts())
                .bulkDomainsSendEmailForNewAccounts(source.isBulkDomainsSendEmailForNewAccounts())
                .bulkDeploymentJobCron(source.getBulkDeploymentJobCron())
                .parallelDeploymentsLimit(source.getParallelDeploymentsLimit())
                .bulkDeploymentQueueRefresh(source.getBulkDeploymentQueueRefresh())
                .bulkDeploymentTimeThreshold(source.getBulkDeploymentTimeThreshold())
                .deploymentPrefix(source.getDeploymentPrefix())
                .healthCheckJobCron(source.getHealthCheckJobCron())
                .defaultDomainForSsoUsers(convertDomain(source.getDefaultDomainForSsoUsers()))
                .build();
    }

    private Long convertDomain(Domain domain) {
        return domain != null ? domain.getId() : null;
    }

}