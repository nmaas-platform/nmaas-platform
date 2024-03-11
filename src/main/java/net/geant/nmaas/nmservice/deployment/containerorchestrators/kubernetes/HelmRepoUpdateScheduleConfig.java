package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.scheduling.ScheduleManager;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Log4j2
public class HelmRepoUpdateScheduleConfig {

    private static final String HELM_REPO_UPDATE_JOB_NAME = "HelmRepoUpdateJob";

    @Bean
    public InitializingBean insertDefaultHelmRepoUpdateJob() {
        return new InitializingBean() {

            @Autowired
            private HelmKServiceManager helmKServiceManager;

            @Autowired
            private HelmRepoUpdateJob helmRepoUpdateJob;

            @Autowired
            private ScheduleManager scheduleManager;

            @Value("${helm.update.async.enabled}")
            private boolean helmRepoUpdateAsyncEnabled;

            @Value("${helm.update.async.cron}")
            private String helmRepoUpdateAsyncCron;

            @Override
            @Transactional
            public void afterPropertiesSet() {
                if (helmRepoUpdateAsyncEnabled) {
                    if (Strings.isNullOrEmpty(helmRepoUpdateAsyncCron)) {
                        log.warn("Asynchronous Helm repo update cron expression not provided.");
                    } else {
                        scheduleManager.createJob(helmRepoUpdateJob, HELM_REPO_UPDATE_JOB_NAME, helmRepoUpdateAsyncCron);
                        // execute helm repo update right away
                        try {
                            helmKServiceManager.updateHelmRepo();
                        } catch (CommandExecutionException e) {
                            log.warn("Wasn't able to execute Helm repo update on startup", e);
                        }
                    }
                } else {
                    log.warn("Asynchronous Helm repo update is disabled.");
                }
            }
        };
    }

}
