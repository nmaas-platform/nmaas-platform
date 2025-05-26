package net.geant.nmaas.externalservices.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterMonitoringJob implements Job {

    private final ClusterMonitoringService clusterMonitoringService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Triggering cluster health check...");
        clusterMonitoringService.updateAllClusterState();
    }

}
