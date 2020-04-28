package net.geant.nmaas.orchestration.tasks.app;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.NmServiceDeployment;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Log4j2
@AllArgsConstructor
public class AppConfigurationTask {

    private NmServiceConfigurationProvider configurationProvider;

    private DefaultAppDeploymentRepositoryManager repositoryManager;

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppApplyConfigurationActionEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = repositoryManager.load(deploymentId);
            final AppDeploymentOwner appDeploymentOwner = repositoryManager.loadOwner(deploymentId);
            configurationProvider.configureNmService(NmServiceDeployment.fromAppDeployment(appDeployment, appDeploymentOwner));
        } catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
