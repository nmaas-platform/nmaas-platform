package net.geant.nmaas.scheduling;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppUpgradeSummaryJob;
import net.geant.nmaas.orchestration.AppUpgradeTriggerJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AppUpgradeScheduleInit implements InitializingBean {

    private static final String APP_UPGRADE_JOB_NAME = "AppUpgradeJob";
    private static final String APP_UPGRADE_SUMMARY_JOB_NAME = "AppUpgradeSummaryJob";

    private final AppUpgradeTriggerJob appUpgradeTriggerJob;
    private final AppUpgradeSummaryJob appUpgradeSummaryJob;
    private final ScheduleManager scheduleManager;
    private final String appUpgradeCron;
    private final String appUpgradeSummaryCron;

    @Autowired
    public AppUpgradeScheduleInit(AppUpgradeTriggerJob appUpgradeTriggerJob,
                                  AppUpgradeSummaryJob appUpgradeSummaryJob,
                                  ScheduleManager scheduleManager,
                                  @Value("${nmaas.service.upgrade.cron}") String appUpgradeCron,
                                  @Value("${nmaas.service.upgrade-summary.cron}") String appUpgradeSummaryCron) {
        this.appUpgradeTriggerJob = appUpgradeTriggerJob;
        this.appUpgradeSummaryJob = appUpgradeSummaryJob;
        this.scheduleManager = scheduleManager;
        this.appUpgradeCron = appUpgradeCron;
        this.appUpgradeSummaryCron = appUpgradeSummaryCron;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(appUpgradeCron)) {
            log.warn("Application upgrade cron expression not provided");
            log.warn("Automatic application upgrades are disabled!");
        } else {
            this.scheduleManager.createJob(appUpgradeTriggerJob, APP_UPGRADE_JOB_NAME, appUpgradeCron);
        }
        if (StringUtils.isEmpty(appUpgradeSummaryCron)) {
            log.warn("Application upgrade summary cron expression not provided");
            log.warn("Won't send out email notifications about automatic upgrades in given period");
        } else {
            this.scheduleManager.createJob(appUpgradeSummaryJob, APP_UPGRADE_SUMMARY_JOB_NAME, appUpgradeSummaryCron);
        }
    }

}