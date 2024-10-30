package net.geant.nmaas.nmservice.deployment.bulks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDeploymentJobService {

    private final AppDeploymentMonitor appDeploymentMonitor;

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private final BulkDeploymentJobRepository bulkDeploymentJobRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${portal.config.bulk.deploy.limit}")
    public Integer deploymentLimitPerMinute;

    public void checkStatusAndDeploy() {
        log.debug("Limit {}", deploymentLimitPerMinute);
        List<BulkDeploymentJobEntry> jobList = bulkDeploymentJobRepository.findAll();

        List<BulkDeploymentJobEntry> toDeploy = jobList.stream().filter(deployment -> appDeploymentMonitor.state(deployment.getIdentifier())
                .equals(AppLifecycleState.REQUESTED)).collect(Collectors.toList());

        log.debug("Jobs to do {}, jobs to deploy {}", jobList.size(), toDeploy.size());

        toDeploy.stream().limit(deploymentLimitPerMinute).forEach( deploy -> {
            eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploy.getIdentifier()));
            log.debug("Trigger running for {}", deploy.getIdentifier());
        });

        deleteRunningJobs();
    }

    public void deleteRunningJobs() {
        List<BulkDeploymentJobEntry> jobList = bulkDeploymentJobRepository.findAll();

        //TODO future for inFailState redeploy application

       jobList.stream().filter(deployment -> {
           AppDeploymentState state =  appDeploymentRepositoryManager.loadState(deployment.getIdentifier());
           return state.isInRunningState() || state.isInFailedState();
       }).peek(dep -> log.debug("Delete job for {}", dep.getIdentifier())).forEach(bulkDeploymentJobRepository::delete);
    }

}
