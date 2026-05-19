package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppUpgradeHistory;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeCompleteEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeFailedEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.repositories.AppUpgradeHistoryRepository;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static net.geant.nmaas.orchestration.AppUpgradeStatus.FAILURE;
import static net.geant.nmaas.orchestration.AppUpgradeStatus.SUCCESS;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppUpgradeTask {

    private static final String DOES_NOT_EXIST = " does not exist";

    private final NmServiceDeploymentProvider serviceDeployment;
    private final ApplicationService applicationService;
    private final ApplicationInstanceService applicationInstanceService;
    private final AppUpgradeHistoryRepository appUpgradeHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeActionEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final Application application = applicationService.findApplication(event.getApplicationId().longValue()).orElseThrow(() -> new InvalidApplicationIdException("Application with id " + event.getApplicationId() + DOES_NOT_EXIST));
            User user = userService.findByUsername(event.getUsername()).orElse(null);
            if (user != null) {
                serviceDeployment.upgradeKubernetesService(
                        deploymentId,
                        event.getAppUpgradeMode(),
                        event.getApplicationId(),
                        application.getAppDeploymentSpec().getKubernetesTemplate(),
                        user.getUsername()
                );
            } else {
                serviceDeployment.upgradeKubernetesService(
                        deploymentId,
                        event.getAppUpgradeMode(),
                        event.getApplicationId(),
                        application.getAppDeploymentSpec().getKubernetesTemplate()
                );
            }
        } catch (Exception ex) {
            logGenericError(ex);
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeCompleteEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            applicationInstanceService.updateApplication(deploymentId, event.getTargetApplicationId().longValue());
            appUpgradeHistoryRepository.save(AppUpgradeHistory.builder().deploymentId(deploymentId).previousApplicationId(event.getPreviousApplicationId()).targetApplicationId(event.getTargetApplicationId()).mode(event.getAppUpgradeMode()).status(SUCCESS).timestamp(new Date()).build());
            sendAppUpgradeNotificationEmail(deploymentId, event.getPreviousApplicationId());
        } catch (Exception ex) {
            logGenericError(ex);
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeFailedEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            appUpgradeHistoryRepository.save(AppUpgradeHistory.builder().deploymentId(deploymentId).previousApplicationId(event.getPreviousApplicationId()).targetApplicationId(event.getTargetApplicationId()).mode(event.getAppUpgradeMode()).status(FAILURE).timestamp(new Date()).build());
        } catch (Exception ex) {
            logGenericError(ex);
        }
    }

    private void sendAppUpgradeNotificationEmail(Identifier deploymentId, Identifier previousApplicationId) {
        final AppInstance appInstance = applicationInstanceService.findByInternalId(deploymentId).orElseThrow(() -> new InvalidApplicationIdException("Application instance with id " + deploymentId.getValue() + DOES_NOT_EXIST));
        final Application previousApplication = applicationService.findApplication(previousApplicationId.longValue()).orElseThrow(() -> new InvalidApplicationIdException("Application with id " + previousApplicationId.getValue() + DOES_NOT_EXIST));
        MailAttributes attributes = MailAttributes.builder().otherAttributes(Map.of("domainName", appInstance.getDomain().getName(), "owner", appInstance.getOwner().getUsername(), "appInstanceName", appInstance.getName(), "appName", appInstance.getApplication().getName(), "appVersion", appInstance.getApplication().getVersion(), "appVersionPrevious", previousApplication.getVersion())).mailType(MailType.APP_UPGRADED).build();
        eventPublisher.publishEvent(new NotificationEvent(this, attributes));
    }

    private void logGenericError(Exception ex) {
        log.error("Error reported at {}", LocalDateTime.now(), ex);
    }

}
