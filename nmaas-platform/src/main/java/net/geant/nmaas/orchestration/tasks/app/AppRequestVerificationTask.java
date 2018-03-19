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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class AppRequestVerificationTask {

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
    public void trigger(AppVerifyRequestActionEvent event) throws InvalidDeploymentIdException, InvalidApplicationIdException, NmServiceRequestVerificationException {
        final Identifier deploymentId = event.getRelatedTo();
        final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        final Application application = appRepository.findOne(Long.valueOf(appDeployment.getApplicationId().getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application for deployment " + deploymentId + " does not exist in repository");
        serviceDeployment.verifyRequest(
                deploymentId,
                appDeployment.getApplicationId(),
                appDeployment.getDomain(),
                application.getAppDeploymentSpec());
    }

}
