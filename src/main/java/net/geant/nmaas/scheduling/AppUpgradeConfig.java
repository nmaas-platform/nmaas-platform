package net.geant.nmaas.scheduling;

import net.geant.nmaas.orchestration.AppUpgradeTriggerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class AppUpgradeConfig {

    private final static String APP_UPGRADE_JOB_NAME = "AppUpgradeJob";

    @Bean
    public InitializingBean insertDefaultAppUpgradeJob() {
        return new InitializingBean() {

            @Autowired
            private AppUpgradeTriggerService appUpgradeTriggerService;

            @Autowired
            private ScheduleManager scheduleManager;

            @Value("${nmaas.service.upgrade.cron}")
            String appUpgradeCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                this.scheduleManager.createJob(appUpgradeTriggerService, APP_UPGRADE_JOB_NAME, appUpgradeCron);
            }
        };
    }

}
