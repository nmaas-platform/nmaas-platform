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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppConfigurationTask {

    private final static Logger log = LogManager.getLogger(AppConfigurationTask.class);

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
    @Loggable(LogLevel.INFO)
    public void applyConfiguration(AppApplyConfigurationActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getRelatedTo();
        final AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        try {
            serviceConfiguration.configureNmService(
                    deploymentId,
                    appDeployment.getApplicationId(),
                    appDeployment.getConfiguration());
        } catch (NmServiceConfigurationFailedException configurationFailedException) {
            log.warn("Service configuration failed for deployment " + deploymentId.value() + " -> " + configurationFailedException.getMessage());
        }
    }

}