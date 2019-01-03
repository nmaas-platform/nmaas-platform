package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.janitor.JanitorService;
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
 */
@Component
public class NmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    private NmServiceConfigurationFilePreparer filePreparer;
    private ConfigurationFileTransferProvider fileTransferor;
    private JanitorService janitorService;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public NmServiceConfigurationExecutor(NmServiceConfigurationFilePreparer filePreparer,
                                          ConfigurationFileTransferProvider fileTransferor,
                                          ApplicationEventPublisher eventPublisher, JanitorService janitorService) {
        this.filePreparer = filePreparer;
        this.fileTransferor = fileTransferor;
        this.eventPublisher = eventPublisher;
        this.janitorService = janitorService;
    }

    /**
     * Triggers configuration files preparation and transfer to destination directory.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param appConfiguration application instance configuration data provided by the user
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     * @throws NmServiceConfigurationFailedException if any error condition occurs
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration,
                                   String namespace, String domain, boolean configFileRepositoryRequired) {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            fileTransferor.transferConfigFiles(deploymentId, configFileIdentifiers, configFileRepositoryRequired);
            if(configFileRepositoryRequired)
                janitorService.createConfigMap(deploymentId, namespace, domain);

            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration, boolean configFileRepositoryRequired){
        try{
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            fileTransferor.updateConfigFiles(deploymentId, configFileIdentifiers, configFileRepositoryRequired);
        } catch(Exception e){
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, ""));
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state, String errorMessage) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, errorMessage));
    }

}
