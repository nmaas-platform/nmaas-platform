package net.geant.nmaas.scheduling;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.RemoteClusterMonitoringJob;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@DependsOn({"portalConfiguration"})
@Slf4j
public class ClusterHealthCheckScheduleInit implements InitializingBean {

    public static final String CLUSTER_HEALTH_CHECK = "ClusterHealthCheck";

    private final RemoteClusterMonitoringJob remoteClusterMonitoringJob;
    private final ScheduleManager scheduleManager;
    private final ConfigurationManager configurationManager;
    private final String healthCheckJobCron;

    @Autowired
    public ClusterHealthCheckScheduleInit(RemoteClusterMonitoringJob remoteClusterMonitoringJob,
                                          ScheduleManager scheduleManager,
                                          ConfigurationManager configurationManager,
                                          @Value("${nmaas.service.health-check.cron}") String healthCheckJobCron) {
        this.remoteClusterMonitoringJob = remoteClusterMonitoringJob;
        this.scheduleManager = scheduleManager;
        this.configurationManager = configurationManager;
        this.healthCheckJobCron = healthCheckJobCron;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        final String healthCheckJobCronFromDb = configurationManager.getConfiguration().getHealthCheckJobCron();
        if (!StringUtils.isEmpty(healthCheckJobCronFromDb)) {
            log.debug("Scheduling cluster health check job based on cron loaded from the database");
            this.scheduleManager.createJob(remoteClusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCronFromDb);
            log.error("Adding new job for health check cluster ...");
        } else if (StringUtils.isEmpty(healthCheckJobCron)) {
            log.warn("Cluster health check cron expression not provided");
        } else {
            log.debug("Scheduling cluster health check job based on cron loaded from properties");
            this.scheduleManager.createJob(remoteClusterMonitoringJob, CLUSTER_HEALTH_CHECK, healthCheckJobCron);
        }

    }
}