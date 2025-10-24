package net.geant.nmaas;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component("portalConfiguration")
@Slf4j
public class PortalConfigInit implements InitializingBean {

    private final boolean maintenance;
    private final boolean ssoLoginAllowed;
    private final String defaultLanguage;
    private final boolean testInstance;
    private final boolean sendAppInstanceFailureEmails;
    private final String appInstanceFailureEmailList;
    private final boolean showDomainRegistrationSelector;
    private final Integer bulkDeploymentPerPeriod;
    private final String bulkDeploymentCron;
    private final String healthCheckJobCron;

    private final ConfigurationManager configurationManager;
    private final ApplicationService applicationService;

    @Autowired
    public PortalConfigInit(@Value("${portal.config.maintenance:false}") boolean maintenance,
                            @Value("${portal.config.ssoLoginAllowed:false}") boolean ssoLoginAllowed,
                            @Value("${portal.config.defaultLanguage:en}") String defaultLanguage,
                            @Value("${portal.config.testInstance:false}") boolean testInstance,
                            @Value("${portal.config.sendAppInstanceFailureEmails:false}") boolean sendAppInstanceFailureEmails,
                            @Value("${portal.config.appInstanceFailureEmailList}") String appInstanceFailureEmailList,
                            @Value("${portal.config.showDomainRegistrationSelector:true}") boolean showDomainRegistrationSelector,
                            @Value("${nmaas.service.deployment.parallel.limit}") Integer bulkDeploymentPerPeriod,
                            @Value("${nmaas.service.bulk-deployment.cron}") String bulkDeploymentCron,
                            @Value("${nmaas.service.health-check.cron}") String healthCheckJobCron,
                            ConfigurationManager configurationManager,
                            ApplicationService applicationService) {
        this.maintenance = maintenance;
        this.ssoLoginAllowed = ssoLoginAllowed;
        this.defaultLanguage = defaultLanguage;
        this.testInstance = testInstance;
        this.sendAppInstanceFailureEmails = sendAppInstanceFailureEmails;
        this.appInstanceFailureEmailList = appInstanceFailureEmailList;
        this.showDomainRegistrationSelector = showDomainRegistrationSelector;
        this.bulkDeploymentPerPeriod = bulkDeploymentPerPeriod;
        this.bulkDeploymentCron = bulkDeploymentCron;
        this.healthCheckJobCron = healthCheckJobCron;
        this.configurationManager = configurationManager;
        this.applicationService = applicationService;
    }

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
            log.debug("[Init] Initializing portal configuration");
            configurationManager.setConfiguration(configurationView);
        } catch (OnlyOneConfigurationSupportedException e) {
            log.debug("Portal configuration already exists. Skipping initialization.");
        }
        log.debug("[Init] Running application configuration templates update");
        applicationService.checkAndUpdateAllConfigurationTemplates();
    }

}
