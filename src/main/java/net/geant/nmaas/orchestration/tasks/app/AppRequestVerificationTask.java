package net.geant.nmaas.orchestration.tasks.app;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Log4j2
public class AppRequestVerificationTask {

    private NmServiceDeploymentProvider serviceDeployment;
    private AppDeploymentRepository repository;
    private ApplicationRepository appRepository;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(AppVerifyRequestActionEvent event) {
        try{
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
            final Application application = appRepository.findById(Long.valueOf(appDeployment.getApplicationId().getValue())).orElseThrow(() ->
                    new InvalidApplicationIdException("Application for deployment " + deploymentId + " does not exist in repository"));
            serviceDeployment.verifyRequest(
                    deploymentId,
                    appDeployment,
                    application.getAppDeploymentSpec());
        } catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
