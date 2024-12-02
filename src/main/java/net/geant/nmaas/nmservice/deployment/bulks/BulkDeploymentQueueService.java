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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        log.debug("Handling bulk queue (total entries {})", queue.size());
        if (queue.isEmpty()) {
            return;
        }

        triggerConfiguration(queue);

        updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue(queue);

        triggerNewDeploymentsFromQueue(queue);
    }

    private void triggerConfiguration(List<BulkDeploymentQueueEntry> queue) {
        queue.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED))
                .forEach(e -> {
                    log.debug("Configuration task triggered for {}", e.getDeploymentId());
                    appLifecycleManager.applyConfiguration(e.getDeploymentId(), AppConfigurationView.builder()
                            .jsonInput(e.getAppConfigurationJson())
                            .mandatoryParameters(e.getAppConfigurationJson()).build(), null);
                });
    }

    private void updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue(List<BulkDeploymentQueueEntry> queue) {
        queue.stream()
                .filter(e -> {
                    AppDeploymentState state =  appDeploymentRepositoryManager.loadState(e.getDeploymentId());
                    return state.isInRunningState() || state.isInFailedState();
                })
                .forEach(e -> {
                    bulkApplicationService.updateEntryStateById(e.getBulkEntryId());
                    log.debug("Removing entry for {}", e.getDeploymentId());
                    queueRepository.delete(e);
                });
    }

    private void triggerNewDeploymentsFromQueue(List<BulkDeploymentQueueEntry> queue) {
        //if application is still being deploying, wait for finish
        if(queue.stream().filter(e -> e.getState().equals(BulkDeploymentQueueEntry.QueryEntryState.IN_PROGRESS)).count() >=  configurationManager.getConfiguration().getParallelDeploymentsLimit() ) {
            log.debug("Application is still being deployed, deploying new application skipped");
        } else {
            queue.stream()
                    .filter(e -> appDeploymentMonitor.state(e.getDeploymentId()).equals(AppLifecycleState.REQUESTED) || e.getState().equals(BulkDeploymentQueueEntry.QueryEntryState.WAITING))
                    .limit(configurationManager.getConfiguration().getParallelDeploymentsLimit()) // we may take into account ongoing deployments as well
                    .forEach(e -> {
                        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, e.getDeploymentId()));
                        e.setState(BulkDeploymentQueueEntry.QueryEntryState.IN_PROGRESS);
                        log.debug("Triggering deployment for {}", e.getDeploymentId());
                    });
        }


    }

}
