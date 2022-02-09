package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.AppUpgradeStatus;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppUpgradeHistory;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeCompleteEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeFailedEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.repositories.AppUpgradeHistoryRepository;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Log4j2
@RequiredArgsConstructor
public class AppUpgradeTask {

    private final NmServiceDeploymentProvider serviceDeployment;
    private final ApplicationService applicationService;
    private final ApplicationInstanceService applicationInstanceService;
    private final AppUpgradeHistoryRepository appUpgradeHistoryRepository;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeActionEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final Application application = applicationService.findApplication(event.getApplicationId().longValue()).orElseThrow(() ->
                    new InvalidApplicationIdException("Application with id " + event.getApplicationId() + " does not exist"));
            serviceDeployment.upgradeKubernetesService(
                    deploymentId,
                    application.getAppDeploymentSpec().getKubernetesTemplate());
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeCompleteEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final Application application = applicationService.findApplication(event.getTargetApplicationId().longValue()).orElseThrow(() ->
                    new InvalidApplicationIdException("Application with id " + event.getTargetApplicationId() + " does not exist"));
            final AppInstance instance = applicationInstanceService.findByInternalId(deploymentId).orElseThrow(() ->
                    new InvalidApplicationIdException("Application instance for deployment " + deploymentId + " does not exist"));
            appUpgradeHistoryRepository.save(AppUpgradeHistory.builder()
                    .deploymentId(deploymentId)
                    .previousApplicationId(event.getPreviousApplicationId())
                    .targetApplicationId(event.getTargetApplicationId())
                    .mode(event.getAppUpgradeMode())
                    .status(AppUpgradeStatus.SUCCESS)
                    .timestamp(new Date()).build());
            applicationInstanceService.updateApplication(instance, application);
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppUpgradeFailedEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            appUpgradeHistoryRepository.save(AppUpgradeHistory.builder()
                    .deploymentId(deploymentId)
                    .previousApplicationId(event.getPreviousApplicationId())
                    .targetApplicationId(event.getTargetApplicationId())
                    .mode(event.getAppUpgradeMode())
                    .status(AppUpgradeStatus.FAILURE)
                    .timestamp(new Date()).build());
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
