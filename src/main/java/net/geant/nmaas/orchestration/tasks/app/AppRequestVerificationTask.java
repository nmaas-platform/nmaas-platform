package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.ServiceRequestVerificationException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.InvalidAttributesException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppRequestVerificationTask {

    private final AppDeploymentRepository repository;
    private final ApplicationRepository applicationRepository;
    private final RemoteClusterManagementService remoteClusterManager;
    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppVerifyRequestActionEvent event) throws InterruptedException {
        Thread.sleep(1000);
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId)
                    .orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
            final Application application = applicationRepository.findById(Long.valueOf(appDeployment.getApplicationId().getValue()))
                    .orElseThrow(() -> new InvalidApplicationIdException("Application for deployment " + deploymentId + " does not exist in repository"));
            if (appDeployment.getRemoteClusterId() != null && !remoteClusterManager.clusterExists(appDeployment.getRemoteClusterId())) {
                throw new InvalidAttributesException("Wrong remote cluster Id");
            }
            serviceDeployment.verifyRequest(
                    deploymentId,
                    appDeployment,
                    application.getAppDeploymentSpec());
        } catch (ServiceRequestVerificationException e) {
            log.warn("New deployment was blocked.");
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
        }
    }

}
