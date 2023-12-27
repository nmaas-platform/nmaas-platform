package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelmRepoUpdateJob implements Job {

    private final HelmKServiceManager helmKServiceManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        helmKServiceManager.updateHelmRepo();
    }

}
