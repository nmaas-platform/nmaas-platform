package net.geant.nmaas.orchestration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class AppUpgradeTriggerService implements Job {

    private final AppDeploymentRepositoryManager deploymentRepositoryManager;
    private final ApplicationInstanceService applicationInstanceService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<AppDeployment> runningDeployments = deploymentRepositoryManager.loadByState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        log.info("Launching automatic application upgrade process");
        log.info("Number of running deployments: {}", runningDeployments.size());
        runningDeployments.stream()
                .map(deployment -> applicationInstanceService.findByInternalId(deployment.getDeploymentId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(AppInstance::isAutoUpgradesEnabled)
                .filter(instance -> applicationInstanceService.checkUpgradePossible(instance.getApplication().getId()))
                .forEach(instance -> {
                    log.debug("Processing application instance: {}/{}", instance.getId(), instance.getInternalId());
                    AppInstanceView.AppInstanceUpgradeInfo upgradeInfo = applicationInstanceService.obtainUpgradeInfo(instance.getApplication().getId());
                    if (Objects.nonNull(upgradeInfo)) {
                        logUpgradeTriggerDetails(instance);
                        eventPublisher.publishEvent(getAppUpgradeActionEvent(instance, upgradeInfo));
                    }
                });
    }

    private void logUpgradeTriggerDetails(AppInstance instance) {
        log.info("Triggering upgrade of instance {} (application: {}, domain: {}, deployment: {})",
                instance.getName(),
                instance.getApplication().getName(),
                instance.getDomain().getName(),
                instance.getInternalId().toString());
    }

    private AppUpgradeActionEvent getAppUpgradeActionEvent(AppInstance instance, AppInstanceView.AppInstanceUpgradeInfo upgradeInfo) {
        return new AppUpgradeActionEvent(this, instance.getInternalId(), Identifier.newInstance(upgradeInfo.getApplicationId()), AppUpgradeMode.AUTO);
    }

}
