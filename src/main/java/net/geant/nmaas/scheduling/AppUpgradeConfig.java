package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppUpgradeSummaryJob;
import net.geant.nmaas.orchestration.AppUpgradeTriggerJob;
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
    private final static String APP_UPGRADE_SUMMARY_JOB_NAME = "AppUpgradeSummaryJob";

    @Bean
    public InitializingBean insertDefaultAppUpgradeJob() {
        return new InitializingBean() {

            @Autowired
            private AppUpgradeTriggerJob appUpgradeTriggerJob;

            @Autowired
            private AppUpgradeSummaryJob appUpgradeSummaryJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Value("${nmaas.service.upgrade.cron}")
            String appUpgradeCron;

            @Value("${nmaas.service.upgrade-summary.cron}")
            String appUpgradeSummaryCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                if (Strings.isNullOrEmpty(appUpgradeCron)) {
                    log.warn("Application upgrade cron expression not provided");
                    log.warn("Automatic application upgrades are disabled!");
                } else {
                    this.scheduleManager.createJob(appUpgradeTriggerJob, APP_UPGRADE_JOB_NAME, appUpgradeCron);
                }
                if (Strings.isNullOrEmpty(appUpgradeSummaryCron)) {
                    log.warn("Application upgrade summary cron expression not provided");
                    log.warn("Won't send out email notifications about automatic upgrades in given period");
                } else {
                    this.scheduleManager.createJob(appUpgradeSummaryJob, APP_UPGRADE_SUMMARY_JOB_NAME, appUpgradeSummaryCron);
                }
            }
        };
    }

}
