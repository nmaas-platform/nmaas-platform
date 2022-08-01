package net.geant.nmaas.orchestration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppUpgradeHistoryRepository;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.events.ApplicationActivatedEvent;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AppUpgradeService {

    private final AppDeploymentRepositoryManager deploymentRepositoryManager;
    private final ApplicationInstanceService applicationInstanceService;
    private final ApplicationService applicationService;
    private final AppUpgradeHistoryRepository appUpgradeHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${nmaas.service.upgrade-summary.interval}")
    Integer appUpgradeSummaryInterval;

    @Transactional
    public void triggerUpgrade() {
        List<AppDeployment> runningDeployments = deploymentRepositoryManager.loadByState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        log.info("Launching automatic application upgrade process");
        log.info("Number of running deployments: {}", runningDeployments.size());
        runningDeployments.stream()
                .map(deployment -> applicationInstanceService.findByInternalId(deployment.getDeploymentId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(AppInstance::isAutoUpgradesEnabled)
                .filter(instance -> applicationInstanceService.checkUpgradePossible(instance.getId()))
                .forEach(instance -> {
                    log.debug("Processing application instance: {}/{}", instance.getId(), instance.getInternalId());
                    AppInstanceView.AppInstanceUpgradeInfo upgradeInfo = applicationInstanceService.obtainUpgradeInfo(instance.getId());
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

    @EventListener
    @Transactional
    public void notifyReadyForUpgrade(ApplicationActivatedEvent event) {
        List<AppDeployment> runningDeployments = deploymentRepositoryManager.loadByState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        log.info("Launching upgrade possibility checks and notifications");
        log.info("Number of running deployments: {}", runningDeployments.size());
        runningDeployments.stream()
                .map(deployment -> applicationInstanceService.findByInternalId(deployment.getDeploymentId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(instance -> instance.getApplication().getName().equals(event.getName()))
                .filter(instance -> applicationInstanceService.checkUpgradePossible(instance.getId()))
                .forEach(instance -> {
                    log.debug("Processing application instance: {}/{}", instance.getId(), instance.getInternalId());
                    AppInstanceView.AppInstanceUpgradeInfo upgradeInfo = applicationInstanceService.obtainUpgradeInfo(instance.getId());
                    if (Objects.nonNull(upgradeInfo)) {
                        MailAttributes attributes = MailAttributes.builder()
                                .otherAttributes(Map.of(
                                        "domainName", instance.getDomain().getName(),
                                        "owner", instance.getOwner().getUsername(),
                                        "appInstanceName", instance.getName(),
                                        "appName", instance.getApplication().getName(),
                                        "appVersion", instance.getApplication().getVersion(),
                                        "appVersionNew", upgradeInfo.getApplicationVersion(),
                                        "appVersionNewChart", upgradeInfo.getHelmChartVersion()
                                ))
                                .mailType(MailType.APP_UPGRADE_POSSIBLE)
                                .build();
                        eventPublisher.publishEvent(new NotificationEvent(this, attributes));
                    }
                });
    }

    @Transactional
    public void notifySummaryAutoUpgraded() {
        log.info("Launching generation of email notification about automatic upgrades that were completed within given period");
        Instant now = Instant.now();
        List<Map> appUpgradesInPeriod = appUpgradeHistoryRepository
                .findInPeriod(Date.from(now.minus(Duration.ofHours(appUpgradeSummaryInterval))), Date.from(now)).stream()
                .map(item -> {
                    AppInstance appInstance = applicationInstanceService.findByInternalId(item.getDeploymentId()).orElseThrow(InvalidDeploymentIdException::new);
                    Application app = applicationService.findApplication(item.getTargetApplicationId().longValue()).orElseThrow(InvalidApplicationIdException::new);
                    return Map.of(
                            "domainName", appInstance.getDomain().getName(),
                            "appInstanceName", appInstance.getName(),
                            "appVersion", app.getVersion(),
                            "appHelmChartVersion", app.getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion(),
                            "timestamp", item.getTimestamp(),
                            "status", item.getStatus().name().toLowerCase()
                    );
                })
                .collect(Collectors.toList());
        MailAttributes attributes = MailAttributes.builder()
                .otherAttributes(Map.of(
                        "summaryInternal", appUpgradeSummaryInterval,
                        "upgradesExist", !appUpgradesInPeriod.isEmpty(),
                        "appUpgrades", appUpgradesInPeriod
                ))
                .mailType(MailType.APP_UPGRADE_SUMMARY)
                .build();
        eventPublisher.publishEvent(new NotificationEvent(this, attributes));
    }

}
