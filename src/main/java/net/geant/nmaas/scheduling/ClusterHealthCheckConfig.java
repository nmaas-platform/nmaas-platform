package net.geant.nmaas.scheduling;


import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.ClusterMonitoringJob;
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
public class ClusterHealthCheckConfig {

    public static final String CLUSTER_HEALTH_CHECK = "ClusterHealthCheck";

    @Bean
    @DependsOn({"portalConfiguration"})
    public InitializingBean insertDefaultClusterHealthCheck() {
        return new InitializingBean() {

            @Autowired
            private ClusterMonitoringJob clusterMonitoringJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Autowired
            private ConfigurationManager configurationManager;

            @Value("${nmaas.service.health-check.cron}")
            String healthCheckJobCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                String healthCheckJobCronDb = configurationManager.getConfiguration().getHealthCheckJobCron();
                if (!Strings.isNullOrEmpty(healthCheckJobCronDb)) {
                    log.debug("Scheduling cluster health check job based on cron loaded from the database");
                    this.scheduleManager.createJob(clusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCron);
                    log.error("Adding new job for health check cluster ...");
                } else if (Strings.isNullOrEmpty(healthCheckJobCron)) {
                    log.warn("Bulk deployment cron expression not provided");
                } else {
                    this.scheduleManager.createJob(clusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCron);
                    log.error("Adding new job for health check cluster ...");
                }

            }
        };
    }

}
