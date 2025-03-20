package net.geant.nmaas.nmservice.deployment.bulks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueEntry.QueryEntryState;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDeploymentQueueService {

    private static final String PROCESSING_TIME = "START_PROCESSING_TIME";

    private final AppDeploymentMonitor appDeploymentMonitor;
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final BulkDeploymentQueueRepository queueRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BulkApplicationService bulkApplicationService;
    private final AppLifecycleManager appLifecycleManager;
    private final BulkDeploymentRepository bulkDeploymentRepository;
    private final ConfigurationManager configurationManager;

    public void handleQueue() {
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        if (queue.isEmpty()) {
            return;
        }
        log.debug("Handling bulk queue (total entries {})", queue.size());

        verifyTimeLimit();
        updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue();
        triggerConfiguration();
        triggerNewDeploymentsFromQueue();
    }

    private void verifyTimeLimit() {
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        queue.stream()
                .filter(e -> !(appDeploymentMonitor.state(e.getDeploymentId()).equals(AppLifecycleState.REQUESTED)
                        || e.getState().equals(QueryEntryState.WAITING)))
                .filter(e -> {
                    AppDeploymentState state = appDeploymentRepositoryManager.loadState(e.getDeploymentId());
                    return !(state.isInRunningState() || state.isInFailedState());
                })
                .forEach(e -> {
                    log.debug("Checking time entry for {}", e.getDeploymentId());
                    Optional<BulkDeploymentEntry> entryOptional = bulkApplicationService.getBulkEntry(e.getBulkEntryId());
                    if (entryOptional.isPresent()) {
                        String startTime = entryOptional.get().getDetails().get(PROCESSING_TIME);
                        long secondsBetween = Duration.between(OffsetDateTime.parse(startTime), OffsetDateTime.now()).getSeconds();
                        if (secondsBetween > configurationManager.getConfiguration().getBulkDeploymentTimeThreshold() * 60) {
                            log.warn("Deployment {} exceeded the time limit for deployment. Entire bulk is going to be canceled.", e.getDeploymentId());
                            cancelOngoingBulkDeployment(e.getBulkEntryId());
                        }
                    }
                });
    }


    private void updateBulkStatusForCompletedOrFailedAndRemoveThemFromQueue() {
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        queue.stream()
                .filter(e -> {
                    AppDeploymentState state = appDeploymentRepositoryManager.loadState(e.getDeploymentId());
                    return state.isInRunningState() || state.isInFailedState();
                })
                .forEach(e -> {
                    bulkApplicationService.updateEntryStateById(e.getBulkEntryId());
                    log.debug("Removing entry for {}", e.getDeploymentId());
                    queueRepository.delete(e);
                });
    }

    private void triggerConfiguration() {
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        queue.stream()
                .filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId()).equals(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED))
                .forEach(e -> {
                    log.debug("Configuration task triggered for {}", e.getDeploymentId());
                    appLifecycleManager.applyConfiguration(e.getDeploymentId(), AppConfigurationView.builder()
                            .jsonInput(e.getAppConfigurationJson())
                            .mandatoryParameters(e.getAppConfigurationJson()).build(), null);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    private void triggerNewDeploymentsFromQueue() {
        List<BulkDeploymentQueueEntry> queue = queueRepository.findAll();
        if (!queue.isEmpty()) {
            long currentBulkId = bulkDeploymentRepository.findBulkIdByBulkEntryId(queue.get(0).getBulkEntryId()); // find bulkId
            long freeCapacity = getFreeCapacity(currentBulkId, queue);
            log.debug("Number of instances that can be triggered right away: {}", freeCapacity);
            queue.stream()
                    .filter(e -> bulkDeploymentRepository.findBulkIdByBulkEntryId(e.getBulkEntryId()).equals(currentBulkId))
                    .filter(e -> appDeploymentMonitor.state(e.getDeploymentId()).equals(AppLifecycleState.REQUESTED)
                            || e.getState().equals(QueryEntryState.WAITING))
                    .limit(freeCapacity)
                    .forEach(e -> {
                        log.debug("Triggering deployment for {}", e.getDeploymentId());
                        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, e.getDeploymentId()));
                        e.setState(QueryEntryState.IN_PROGRESS);
                        bulkApplicationService.setBulkEntryToProcessing(e.getBulkEntryId());
                        queueRepository.save(e);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }

    private long getFreeCapacity(long currentBulkId, List<BulkDeploymentQueueEntry> queue) {
        Integer globalLimit = configurationManager.getConfiguration().getParallelDeploymentsLimit();
        Integer bulkLimit = bulkDeploymentRepository.findParallelDeploymentsLimitByBulkId(currentBulkId);
        long parallelDeploymentsLimit = globalLimit >= bulkLimit ? bulkLimit : globalLimit;
        log.debug("Calculated limit: {} ", parallelDeploymentsLimit);
        long ongoingDeployments = queue.stream().filter(e -> e.getState().equals(QueryEntryState.IN_PROGRESS)).count();
        return parallelDeploymentsLimit - ongoingDeployments;
    }

    private void cancelOngoingBulkDeployment(Long bulkEntryId) {
        BulkDeployment bulkDeployment = bulkDeploymentRepository.findByBulkEntryId(bulkEntryId);
        List<Long> entriesId = bulkDeployment.getEntries().stream()
                .map(BulkDeploymentEntry::getId)
                .toList();
        entriesId.forEach(entry -> {
            queueRepository.findAll().forEach(each ->
                    log.warn("In queue: {} {} {}", each.getId(), each.getBulkEntryId(), each.getDeploymentId())
            );
            Optional<BulkDeploymentQueueEntry> optional = queueRepository.findByBulkEntryId(entry);
            log.warn("Found from bulk {} entries {} in queue. Deleting .. ? {}", bulkDeployment.getId(), entry, optional.isPresent());
            if (optional.isPresent()) {
                log.warn("Delete {} / {}", optional.get().getDeploymentId(), optional.get().getBulkEntryId());
                queueRepository.deleteById(optional.get().getId());
                bulkApplicationService.setBulkToCancel(optional.get());
            }
        });
        bulkApplicationService.updateMainState(bulkDeployment);
    }


}
