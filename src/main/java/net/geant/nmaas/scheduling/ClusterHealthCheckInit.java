package net.geant.nmaas.scheduling;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.ClusterMonitoringJob;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@DependsOn({"portalConfiguration"})
@Slf4j
public class ClusterHealthCheckInit implements InitializingBean {

    public static final String CLUSTER_HEALTH_CHECK = "ClusterHealthCheck";

    private final ClusterMonitoringJob clusterMonitoringJob;
    private final ScheduleManager scheduleManager;
    private final ConfigurationManager configurationManager;
    private final String healthCheckJobCron;

    @Autowired
    public ClusterHealthCheckInit(ClusterMonitoringJob clusterMonitoringJob,
                                  ScheduleManager scheduleManager,
                                  ConfigurationManager configurationManager,
                                  @Value("${nmaas.service.health-check.cron}") String healthCheckJobCron) {
        this.clusterMonitoringJob = clusterMonitoringJob;
        this.scheduleManager = scheduleManager;
        this.configurationManager = configurationManager;
        this.healthCheckJobCron = healthCheckJobCron;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        final String healthCheckJobCronFromDb = configurationManager.getConfiguration().getHealthCheckJobCron();
        if (!Strings.isNullOrEmpty(healthCheckJobCronFromDb)) {
            log.debug("Scheduling cluster health check job based on cron loaded from the database");
            this.scheduleManager.createJob(clusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCronFromDb);
            log.error("Adding new job for health check cluster ...");
        } else if (Strings.isNullOrEmpty(healthCheckJobCron)) {
            log.warn("Cluster health check cron expression not provided");
        } else {
            log.debug("Scheduling cluster health check job based on cron loaded from properties");
            this.scheduleManager.createJob(clusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCron);
        }

    }
}