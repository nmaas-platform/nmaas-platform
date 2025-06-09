package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.scheduling.ScheduleManager;
import net.geant.nmaas.utils.bash.CommandExecutionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class HelmRepoUpdateScheduleConfig implements InitializingBean {

    private static final String HELM_REPO_UPDATE_JOB_NAME = "HelmRepoUpdateJob";

    private final HelmKServiceManager helmKServiceManager;
    private final HelmRepoUpdateJob helmRepoUpdateJob;
    private final ScheduleManager scheduleManager;
    private final boolean helmRepoUpdateAsyncEnabled;
    private final String helmRepoUpdateAsyncCron;

    @Autowired
    public HelmRepoUpdateScheduleConfig(HelmKServiceManager helmKServiceManager,
                                        HelmRepoUpdateJob helmRepoUpdateJob,
                                        ScheduleManager scheduleManager,
                                        @Value("${helm.update.async.enabled}") boolean helmRepoUpdateAsyncEnabled,
                                        @Value("${helm.update.async.cron}") String helmRepoUpdateAsyncCron) {
        this.helmKServiceManager = helmKServiceManager;
        this.helmRepoUpdateJob = helmRepoUpdateJob;
        this.scheduleManager = scheduleManager;
        this.helmRepoUpdateAsyncEnabled = helmRepoUpdateAsyncEnabled;
        this.helmRepoUpdateAsyncCron = helmRepoUpdateAsyncCron;
    }

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

}
