package net.geant.nmaas.orchestration.tasks.app;

import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppConfigurationUpdateTask {

    private NmServiceConfigurationProvider configurationProvider;
    private AppDeploymentRepositoryManager repositoryManager;

    @Autowired
    public AppConfigurationUpdateTask(NmServiceConfigurationProvider configurationProvider,
                                      AppDeploymentRepositoryManager repositoryManager){
        this.configurationProvider = configurationProvider;
        this.repositoryManager = repositoryManager;
    }

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppUpdateConfigurationEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
            configurationProvider.updateNmService(deploymentId,
                    appDeployment.getApplicationId(),
                    appDeployment.getConfiguration(),
                    appDeployment.getDomain(),
                    appDeployment.isConfigFileRepositoryRequired());
        } catch (Exception e){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, e);
        }
    }
}
