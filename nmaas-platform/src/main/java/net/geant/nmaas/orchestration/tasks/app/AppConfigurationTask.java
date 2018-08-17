package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class AppConfigurationTask {

    private NmServiceConfigurationProvider serviceConfiguration;

    private AppDeploymentRepositoryManager repositoryManager;

    @Autowired
    public AppConfigurationTask(
            NmServiceConfigurationProvider serviceConfiguration,
            AppDeploymentRepositoryManager repositoryManager) {
        this.serviceConfiguration = serviceConfiguration;
        this.repositoryManager = repositoryManager;
    }

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppApplyConfigurationActionEvent event) throws InvalidDeploymentIdException, NmServiceConfigurationFailedException {
        final Identifier deploymentId = event.getRelatedTo();
        final AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        serviceConfiguration.configureNmService(
                deploymentId,
                appDeployment.getApplicationId(),
                appDeployment.getConfiguration());
}

}
