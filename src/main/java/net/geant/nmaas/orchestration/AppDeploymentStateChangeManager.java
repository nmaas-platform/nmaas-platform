package net.geant.nmaas.orchestration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent.EventDetailType;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeCompleteEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeFailedEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.portal.events.ApplicationDeployedEvent;
import net.geant.nmaas.portal.events.ApplicationRemovedEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppDeploymentStateChangeManager {

    private final DefaultAppDeploymentRepositoryManager deploymentRepositoryManager;
    private final AppDeploymentMonitor deploymentMonitor;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized ApplicationEvent notifyStateChange(NmServiceDeploymentStateChangeEvent event) {
        try {
            AppDeploymentState newDeploymentState = deploymentRepositoryManager.loadState(event.getDeploymentId()).nextState(event.getState());
            deploymentRepositoryManager.updateState(event.getDeploymentId(), newDeploymentState);
            if (newDeploymentState.isInFailedState()) {
                log.warn("Application deployment failed state detected. Saving error message: {}", event.getErrorMessage());
                deploymentRepositoryManager.updateErrorMessage(event.getDeploymentId(), event.getErrorMessage());
                eventPublisher.publishEvent(
                        new NotificationEvent(this, getMailAttributes(deploymentRepositoryManager.load(event.getDeploymentId()), event.getErrorMessage()))
                );
                if (newDeploymentState == AppDeploymentState.APPLICATION_UPGRADE_FAILED) {
                    eventPublisher.publishEvent(
                            new AppUpgradeFailedEvent(this,
                                    event.getDeploymentId(),
                                    deploymentRepositoryManager.loadApplicationId(event.getDeploymentId()),
                                    Identifier.newInstance(event.getDetail(EventDetailType.NEW_APPLICATION_ID)),
                                    AppUpgradeMode.valueOf(event.getDetail(EventDetailType.UPGRADE_TRIGGER_TYPE)))
                    );
                }
            }
            if (newDeploymentState == AppDeploymentState.APPLICATION_UPGRADED) {
                Identifier previousApplicationId = deploymentRepositoryManager.loadApplicationId(event.getDeploymentId());
                Identifier newApplicationId = Identifier.newInstance(event.getDetail(EventDetailType.NEW_APPLICATION_ID));
                deploymentRepositoryManager.updateApplicationId(event.getDeploymentId(), newApplicationId);
                eventPublisher.publishEvent(
                        new AppUpgradeCompleteEvent(this,
                                event.getDeploymentId(),
                                previousApplicationId,
                                newApplicationId,
                                AppUpgradeMode.valueOf(event.getDetail(EventDetailType.UPGRADE_TRIGGER_TYPE)))
                );
            }
            if (newDeploymentState == AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED
                    && deploymentRepositoryManager.isFirstTimeDeployment(event.getDeploymentId())) {
                eventPublisher.publishEvent(
                        new NotificationEvent(this, getMailAttributes(deploymentRepositoryManager.load(event.getDeploymentId()))));
                eventPublisher.publishEvent(
                        new ApplicationDeployedEvent(this, event.getDeploymentId().toString()));
            }
            if (newDeploymentState == AppDeploymentState.APPLICATION_REMOVED) {
                eventPublisher.publishEvent(
                        new ApplicationRemovedEvent(this, event.getDeploymentId().toString()));
            }
            return triggerActionEventIfRequired(event.getDeploymentId(), newDeploymentState).orElse(null);
        } catch (InvalidAppStateException e) {
            log.warn("State notification failure -> {}", e.getMessage());
            deploymentRepositoryManager.updateErrorMessage(event.getDeploymentId(), e.getMessage());
            deploymentRepositoryManager.updateState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
            eventPublisher.publishEvent(
                    new NotificationEvent(this, getMailAttributes(deploymentRepositoryManager.load(event.getDeploymentId()), e.getMessage()))
            );
            return null;
        }
    }

    Optional<ApplicationEvent> triggerActionEventIfRequired(Identifier deploymentId, AppDeploymentState currentState) {
        return switch (currentState) {
            case REQUEST_VALIDATED -> Optional.of(new AppPrepareEnvironmentActionEvent(this, deploymentId));
            case DEPLOYMENT_ENVIRONMENT_PREPARED ->
                    Optional.of(new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId));
            case MANAGEMENT_VPN_CONFIGURED -> Optional.of(new AppVerifyConfigurationActionEvent(this, deploymentId));
            case APPLICATION_CONFIGURED -> Optional.of(new AppDeployServiceActionEvent(this, deploymentId));
            case APPLICATION_DEPLOYED, APPLICATION_RESTARTED, APPLICATION_UPGRADED, APPLICATION_CONFIGURATION_UPDATED,
                 APPLICATION_RESUMED -> Optional.of(new AppVerifyServiceActionEvent(this, deploymentId));
            case APPLICATION_REMOVED -> Optional.of(new AppRemoveDcnIfRequiredEvent(this, deploymentId));
            default -> Optional.empty();
        };
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized void notifyDcnDeployed(DcnDeployedEvent event) {
        try {
            deploymentRepositoryManager.loadAllWaitingForDcn(event.getRelatedTo())
                    .forEach(d -> eventPublisher.publishEvent(
                            new NmServiceDeploymentStateChangeEvent(this, d.getDeploymentId(), ServiceDeploymentState.READY_FOR_DEPLOYMENT, "")));
        } catch (Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at {}", timestamp, ex);
        }
    }

    private MailAttributes getMailAttributes(AppDeployment appDeployment) {
        return MailAttributes.builder()
                .otherAttributes(Map.of(
                        "accessURL", prepareDeployUrl(appDeployment),
                        "domainName", deploymentRepositoryManager.loadDomainName(appDeployment.getDeploymentId()),
                        "owner", appDeployment.getOwner(),
                        "appInstanceName", appDeployment.getDeploymentName(),
                        "appName", appDeployment.getAppName()
                ))
                .mailType(MailType.APP_DEPLOYED)
                .build();
    }

    private String prepareDeployUrl(AppDeployment appDeployment) {
        String url = deploymentMonitor.userAccessDetails(appDeployment.getDeploymentId())
                .getServiceAccessMethods().stream()
                .filter(m -> !Arrays.asList(ServiceAccessMethodTypeDto.INTERNAL, ServiceAccessMethodTypeDto.LOCAL).contains(m.getType()))
                .map(ServiceAccessMethodDto::getUrl)
                .findFirst()
                .orElse("");

        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            url = "https://" + url;
        }
        return url;
    }

    private MailAttributes getMailAttributes(AppDeployment appDeployment, String error) {
        return MailAttributes.builder()
                .otherAttributes(Map.of(
                        "domainName", deploymentRepositoryManager.loadDomainName(appDeployment.getDeploymentId()),
                        "owner", appDeployment.getOwner(),
                        "appInstanceName", appDeployment.getDeploymentName(),
                        "appName", appDeployment.getAppName(),
                        "error", error
                ))
                .mailType(MailType.APP_DEPLOYMENT_FAILED)
                .build();
    }

}
