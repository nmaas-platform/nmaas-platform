package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carries out the service configuration process.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class ServiceConfigurationExecutor {

    @Autowired
    private NmServiceConfigurationFilePreparer filePreparer;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Triggers configuration files preparation and transfer to destination directory.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param appConfiguration application instance configuration data provided by the user
     * @param fileTransfer {@link ConfigurationFileTransferProvider} implementation to be used for file transfer
     * @throws NmServiceConfigurationFailedException if any error condition occurs
     */
    public void configure(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration, ConfigurationFileTransferProvider fileTransfer)
            throws NmServiceConfigurationFailedException {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            fileTransfer.transferConfigFiles(deploymentId, configFileIdentifiers);
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
