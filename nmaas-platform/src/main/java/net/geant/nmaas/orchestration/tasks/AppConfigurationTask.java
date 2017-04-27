package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

    private NmServiceRepositoryManager nmServiceRepositoryManager;

    private AppDeploymentRepository repository;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AppConfigurationTask(
            NmServiceConfigurationProvider serviceConfiguration,
            NmServiceRepositoryManager nmServiceRepositoryManager,
            AppDeploymentRepository repository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.serviceConfiguration = serviceConfiguration;
        this.nmServiceRepositoryManager = nmServiceRepositoryManager;
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void applyConfiguration(AppApplyConfigurationActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        final NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
        try {
            serviceConfiguration.configureNmService(
                    deploymentId,
                    appDeployment.getApplicationId(),
                    appDeployment.getConfiguration(),
                    service.getHost(),
                    service.getDockerContainer().getVolumesDetails());
        } catch (NmServiceConfigurationFailedException configurationFailedException) {
            log.warn("Service configuration failed for deployment " + deploymentId.value() + " -> " + configurationFailedException.getMessage());
        }
    }

}
