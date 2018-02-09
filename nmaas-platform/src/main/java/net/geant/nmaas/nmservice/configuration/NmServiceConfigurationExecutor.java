package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default implementation of the {@link NmServiceConfigurationProvider} interface.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class NmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    private NmServiceConfigurationFilePreparer filePreparer;
    private ConfigurationFileTransferProvider fileTransferor;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public NmServiceConfigurationExecutor(NmServiceConfigurationFilePreparer filePreparer, ConfigurationFileTransferProvider fileTransferor, ApplicationEventPublisher eventPublisher) {
        this.filePreparer = filePreparer;
        this.fileTransferor = fileTransferor;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Triggers configuration files preparation and transfer to destination directory.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param appConfiguration application instance configuration data provided by the user
     * @throws NmServiceConfigurationFailedException if any error condition occurs
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration)
            throws NmServiceConfigurationFailedException {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            fileTransferor.transferConfigFiles(deploymentId, configFileIdentifiers);
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
