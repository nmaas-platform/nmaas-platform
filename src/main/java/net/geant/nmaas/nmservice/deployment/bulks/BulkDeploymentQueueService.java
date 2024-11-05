package net.geant.nmaas.nmservice.deployment.bulks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDeploymentQueueService {

    private final AppDeploymentMonitor appDeploymentMonitor;
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final BulkDeploymentQueueRepository queueRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BulkApplicationService bulkApplicationService;
    private final AppLifecycleManager appLifecycleManager;

   private final ConfigurationManager configurationManager;

    public void handleQueue() {
        log.debug("Limit {}", configurationManager.getConfiguration().getParallelDeploymentsLimit());
        List<BulkDeploymentQueueEntry> jobList = queueRepository.findAll();

        List<BulkDeploymentQueueEntry> toDeploy = jobList.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.REQUESTED)).collect(Collectors.toList());

        //app to configure
        jobList.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED)).forEach( deployment -> {
                    log.debug("Configuration triggered for {}", deployment.getDeploymentId());
            appLifecycleManager.applyConfiguration(deployment.getDeploymentId(), AppConfigurationView.builder()
                    .jsonInput(deployment.getAppConfigurationJson())
                    .mandatoryParameters(deployment.getAppConfigurationJson()).build(), null);
        });

        log.debug("Jobs to do {}, jobs to deploy {}", jobList.size(), toDeploy.size());

        toDeploy.stream().limit(configurationManager.getConfiguration().getParallelDeploymentsLimit()).forEach( deploy -> {
            eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploy.getDeploymentId()));
            log.debug("Trigger running for {}", deploy.getDeploymentId());
        });

        removeCompletedFromQueue();
    }

    private void removeCompletedFromQueue() {
        List<BulkDeploymentQueueEntry> jobList = queueRepository.findAll();

        // TODO future for inFailState redeploy application

        jobList.stream().filter(deployment -> {
            AppDeploymentState state =  appDeploymentRepositoryManager.loadState(deployment.getDeploymentId());
            return state.isInRunningState() || state.isInFailedState();
        }).peek(dep -> log.debug("Delete job for {}", dep.getDeploymentId())).forEach(dep -> {
            bulkApplicationService.updateEntryStateById(dep.getBulkEntryId());
            queueRepository.delete(dep);
        });
    }

    public void updateBulkStatus() {
        this.bulkApplicationService.updateBulkApplicationStatus();
    }

}
