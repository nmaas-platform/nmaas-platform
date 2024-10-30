package net.geant.nmaas.nmservice.deployment.bulks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BulkDeploymentJob implements Job {

    private final BulkDeploymentJobService service ;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Trigger bulk deployment job...");
        service.checkStatusAndDeploy();

    }
}
