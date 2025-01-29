package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJob;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class BulkDeploymentScheduleConfig {

    public static final String BULK_DEPLOYMENT_JOB = "BulkDeploymentJob";

    @Bean
    @DependsOn({"portalConfiguration"})
    public InitializingBean insertDefaultBulkDeploymentJob() {
        return new InitializingBean() {

            @Autowired
            private BulkDeploymentJob bulkDeploymentJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Autowired
            private ConfigurationManager configurationManager;

            @Value("${nmaas.service.bulk-deployment.cron}")
            String bulkDeploymentCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                String bulkDeploymentCronFromDb = configurationManager.getConfiguration().getBulkDeploymentJobCron();
                if (!Strings.isNullOrEmpty(bulkDeploymentCronFromDb)) {
                    log.debug("Scheduling bulk deployment job based on cron loaded from the database");
                    this.scheduleManager.createJob(bulkDeploymentJob, BULK_DEPLOYMENT_JOB, bulkDeploymentCronFromDb);
                } else if (Strings.isNullOrEmpty(bulkDeploymentCron)) {
                    log.warn("Bulk deployment cron expression not provided");
                } else {
                    this.scheduleManager.createJob(bulkDeploymentJob, BULK_DEPLOYMENT_JOB, bulkDeploymentCron);
                }
            }
        };
    }

}