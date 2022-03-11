package net.geant.nmaas.orchestration;

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUpgradeTriggerJob implements Job {

    private final AppUpgradeService appUpgradeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        appUpgradeService.triggerUpgrade();
    }

}
