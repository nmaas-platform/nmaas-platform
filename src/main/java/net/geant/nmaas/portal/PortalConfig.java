package net.geant.nmaas.portal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PortalConfig {

    @Bean("portalConfiguration")
    public InitializingBean saveDefaultPortalConfiguration() {
        return new InitializingBean() {

            @Value("${portal.config.maintenance:false}")
            private boolean maintenance;

            @Value("${portal.config.ssoLoginAllowed:false}")
            private boolean ssoLoginAllowed;

            @Value("${portal.config.defaultLanguage}")
            private String defaultLanguage = "en";

            @Value("${portal.config.testInstance:false}")
            private boolean testInstance;

            @Value("${portal.config.sendAppInstanceFailureEmails:false}")
            private boolean sendAppInstanceFailureEmails;

            @Value("${portal.config.appInstanceFailureEmailList}")
            private String appInstanceFailureEmailList;

            @Value("${portal.config.showDomainRegistrationSelector:true}")
            private boolean showDomainRegistrationSelector;

            @Value("${nmaas.service.deployment.parallel.limit}")
            Integer bulkDeploymentPerPeriod;

            @Value("${nmaas.service.bulk-deployment.cron}")
            String bulkDeploymentCron;

            @Value("${nmaas.service.health-check.cron}")
            String healthCheckJobCron;

            @Autowired
            private ConfigurationManager configurationManager;

            @Autowired
            private ApplicationService applicationService;

            @Override
            public void afterPropertiesSet() {
                ConfigurationView configurationView = ConfigurationView.builder()
                        .maintenance(this.maintenance)
                        .ssoLoginAllowed(this.ssoLoginAllowed)
                        .defaultLanguage(this.defaultLanguage)
                        .testInstance(this.testInstance)
                        .sendAppInstanceFailureEmails(this.sendAppInstanceFailureEmails)
                        .appInstanceFailureEmailList(Arrays.asList(this.appInstanceFailureEmailList.split(";")))
                        .registrationDomainSelectionEnabled(this.showDomainRegistrationSelector)
                        .bulkDeploymentJobCron(bulkDeploymentCron)
                        .parallelDeploymentsLimit(bulkDeploymentPerPeriod)
                        .bulkDeploymentTimeThreshold(600)
                        .bulkDeploymentQueueRefresh(60)
                        .deploymentPrefix(UUID.randomUUID().toString().substring(0, 3))
                        .healthCheckJobCron(healthCheckJobCron)
                        .build();
                try {
                    configurationManager.setConfiguration(configurationView);
                } catch (OnlyOneConfigurationSupportedException e) {
                    log.debug("Portal configuration already exists. Skipping initialization.");
                }

                log.debug("Running application configuration templates update");
                applicationService.checkAndUpdateAllConfigurationTemplates();
            }
        };
    }
}
