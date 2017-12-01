package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppRequestVerificationTask {

    private final static Logger log = LogManager.getLogger(AppRequestVerificationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private AppDeploymentRepository repository;

    private ApplicationRepository appRepository;

    @Autowired
    public AppRequestVerificationTask(
            NmServiceDeploymentProvider serviceDeployment,
            AppDeploymentRepository repository,
            ApplicationRepository appRepository) {
        this.serviceDeployment = serviceDeployment;
        this.repository = repository;
        this.appRepository = appRepository;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verifyAppRequest(AppVerifyRequestActionEvent event) throws InvalidDeploymentIdException, InvalidApplicationIdException {
        final Identifier deploymentId = event.getRelatedTo();
        final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        final Application application = appRepository.findOne(Long.valueOf(appDeployment.getApplicationId().getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application for deployment " + deploymentId + " does not exist in repository");
        try {
            serviceDeployment.verifyRequest(
                    deploymentId,
                    appDeployment.getApplicationId(),
                    appDeployment.getClientId(),
                    application.getAppDeploymentSpec());
        } catch (NmServiceRequestVerificationException e) {
            log.warn("Service request verification failed for deployment " + deploymentId + " -> " + e.getMessage());
        }
    }

}
