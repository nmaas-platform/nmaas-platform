package net.geant.nmaas.externalservices.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.quartz.Job;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterMonitoringJob implements Job {

    private final RemoteClusterManager remoteClusterManager;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Triggering cluster health check...");
        remoteClusterManager.restoreFileIfMissing();
        log.info("File checked, everything looks fine. Next stage: Update clusters state.");
        remoteClusterManager.updateAllClusterState();
    }

}
