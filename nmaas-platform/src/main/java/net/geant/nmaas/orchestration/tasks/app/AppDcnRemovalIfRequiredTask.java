package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppDcnRemovalIfRequiredTask {

    private DefaultAppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @Autowired
    public AppDcnRemovalIfRequiredTask(DefaultAppDeploymentRepositoryManager appDeploymentRepositoryManager){
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent trigger(AppRemoveDcnIfRequiredEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final String domain = appDeploymentRepositoryManager.loadDomain(deploymentId);
            //TODO: refactor method to check if the DCN can be automatically removed
            return null;
        } catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
            return null;
        }
    }

}
