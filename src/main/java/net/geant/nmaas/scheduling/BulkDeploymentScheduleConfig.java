package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJob;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Log4j2
public class BulkDeploymentScheduleConfig {

    private static final String BULK_DEPLOYMENT_JOB = "BulkDeploymentJob";

    @Bean
    public InitializingBean insertDefaultBulkDeploymentJob() {
        return new InitializingBean() {

            @Autowired
            private BulkDeploymentJob bulkDeploymentJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Value("${nmaas.service.bulk-deployment.cron}")
            String bulkDeploymentCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                if (Strings.isNullOrEmpty(bulkDeploymentCron)) {
                    log.warn("Bulk deployment cron expression not provided");
                } else {
                    this.scheduleManager.createJob(bulkDeploymentJob, BULK_DEPLOYMENT_JOB, bulkDeploymentCron);
                }
            }
        };
    }

}
