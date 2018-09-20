package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyConfigurationActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppConfigurationVerificationTask {

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public AppConfigurationVerificationTask(AppDeploymentRepositoryManager appDeploymentRepositoryManager, ApplicationEventPublisher eventPublisher){
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppVerifyConfigurationActionEvent event) throws InvalidDeploymentIdException {
        try{
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = appDeploymentRepositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
            if(appDeployment.getConfiguration() != null && appDeployment.getStorageSpace() != null){
                eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
            }
        } catch(Exception e){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, e);
        }
    }

    private NmServiceDeploymentStateChangeEvent applyConfigurationEvent(Identifier deploymentId){
        return new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
    }

}
