package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppRemoveFailedActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppRemoveFailedActionTask {
    private NmServiceDeploymentProvider serviceDeployment;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public AppRemoveFailedActionTask(NmServiceDeploymentProvider serviceDeployment, ApplicationEventPublisher eventPublisher) {
        this.serviceDeployment = serviceDeployment;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppRemoveFailedActionEvent event) {
        try{
            //TODO
            eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, event.getRelatedTo(), NmServiceDeploymentState.FAILED_APPLICATION_REMOVED, ""));
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }
}