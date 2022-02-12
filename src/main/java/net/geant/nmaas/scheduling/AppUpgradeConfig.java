package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppUpgradeTriggerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Log4j2
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
                if (Strings.isNullOrEmpty(appUpgradeCron)) {
                    log.warn("Application upgrade cron expression not provided");
                    log.warn("Automatic application upgrades are disabled!");
                    return;
                }
                this.scheduleManager.createJob(appUpgradeTriggerService, APP_UPGRADE_JOB_NAME, appUpgradeCron);
            }
        };
    }

}
