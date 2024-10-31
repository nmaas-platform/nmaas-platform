package net.geant.nmaas.nmservice.deployment.bulks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.portal.service.BulkApplicationService;
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

    @Value("${nmaas.service.deployment.parallel.limit}")
    public Integer parallelDeploymentsLimit;

    public void handleQueue() {
        log.debug("Limit {}", parallelDeploymentsLimit);
        List<BulkDeploymentQueueEntry> jobList = queueRepository.findAll();

        List<BulkDeploymentQueueEntry> toDeploy = jobList.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getDeploymentId())
                .equals(AppLifecycleState.REQUESTED)).collect(Collectors.toList());

        log.debug("Jobs to do {}, jobs to deploy {}", jobList.size(), toDeploy.size());

        toDeploy.stream().limit(parallelDeploymentsLimit).forEach( deploy -> {
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
