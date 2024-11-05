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
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        log.debug("Handling bulk queue (total entries {})", queue.size());

        List<BulkDeploymentQueueEntry> toDeploy = queue.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.REQUESTED)).collect(Collectors.toList());

        //app to configure
        queue.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED)).forEach( deployment -> {
                    log.debug("Configuration triggered for {}", deployment.getDeploymentId());
            appLifecycleManager.applyConfiguration(deployment.getDeploymentId(), AppConfigurationView.builder()
                    .jsonInput(deployment.getAppConfigurationJson())
                    .mandatoryParameters(deployment.getAppConfigurationJson()).build(), null);
        });


        toDeploy.stream().limit(configurationManager.getConfiguration().getParallelDeploymentsLimit()).forEach( deploy -> {
            eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploy.getDeploymentId()));
            log.debug("Trigger running for {}", deploy.getDeploymentId());
        });
        updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue(queue);

        triggerNewDeploymentsFromQueue(queue);
    }

    private void updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue(List<BulkDeploymentQueueEntry> queue) {
        log.debug("Verifying statuses and cleaning up the queue");
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
        queue.stream()
                .filter(e -> appDeploymentMonitor.state(e.getDeploymentId()).equals(AppLifecycleState.REQUESTED))
                .limit(parallelDeploymentsLimit) // we may take into account ongoing deployments as well
                .forEach(e -> {
                    eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, e.getDeploymentId()));
                    log.debug("Trigger running for {}", e.getDeploymentId());
                });
    }

}
