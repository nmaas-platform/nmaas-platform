package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJob;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJobService;
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
public class AppUpgradeScheduleConfig {

    private static final String APP_UPGRADE_JOB_NAME = "AppUpgradeJob";
    private static final String APP_UPGRADE_SUMMARY_JOB_NAME = "AppUpgradeSummaryJob";
    private static final String BULK_DEPLOYMENT_JOB = "BulkDeploymentJob";


    @Bean
    public InitializingBean insertDefaultAppUpgradeJob() {
        return new InitializingBean() {

            @Autowired
            private AppUpgradeTriggerJob appUpgradeTriggerJob;

            @Autowired
            private AppUpgradeSummaryJob appUpgradeSummaryJob;

            @Autowired
            private BulkDeploymentJob bulkDeploymentJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Value("${nmaas.service.upgrade.cron}")
            String appUpgradeCron;

            @Value("${nmaas.service.upgrade-summary.cron}")
            String appUpgradeSummaryCron;

            @Value("${nmaas.service.bulk-deployment.cron}")
            String bulkDeploymentCron;

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
                if (Strings.isNullOrEmpty(bulkDeploymentCron)) {
                    log.warn("Bulk deployment cron expression not provided");
                } else {
                    log.warn("Created bulk deplyoment cron ");
                    this.scheduleManager.createJob(bulkDeploymentJob, BULK_DEPLOYMENT_JOB, bulkDeploymentCron);
                }
            }
        };
    }

}
