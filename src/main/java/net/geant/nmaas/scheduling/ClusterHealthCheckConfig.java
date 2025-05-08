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

            @Value("${nmaas.service.bulk-deployment.cron}")
            String bulkDeploymentCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                String bulkDeploymentCronFromDb = configurationManager.getConfiguration().getBulkDeploymentJobCron();
                log.error("Adding new job for health check cluster ...");
                this.scheduleManager.createJob(clusterMonitoringJob, CLUSTER_HEALTH_CHECK, bulkDeploymentCronFromDb);
            }
        };
    }

}
