package net.geant.nmaas.nmservice.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_REMOVAL_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_REMOVAL_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_REMOVED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_UPDATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_UPDATE_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURATION_UPDATE_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.CONFIGURED;

/**
 * Default implementation of the {@link NmServiceConfigurationProvider} interface.
 */
@Component
@AllArgsConstructor
@Log4j2
public class NmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    private NmServiceConfigurationFilePreparer filePreparer;
    private ConfigurationFileTransferProvider fileUploader;
    private JanitorService janitorService;
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier descriptiveDeploymentId, Identifier applicationId, AppConfiguration appConfiguration,
                                   String domain, boolean configFileRepositoryRequired) {
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            fileUploader.transferConfigFiles(deploymentId, descriptiveDeploymentId, configFileIdentifiers, configFileRepositoryRequired);
            if(configFileRepositoryRequired) {
                janitorService.createOrReplaceConfigMap(descriptiveDeploymentId, domain);
            }
            notifyStateChangeListeners(deploymentId, CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateNmService(Identifier deploymentId, Identifier descriptiveDeploymentId, Identifier applicationId, AppConfiguration appConfiguration,
                                String domain, boolean configFileRepositoryRequired){
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_INITIATED);
            log.debug("Generating updated configuration files ...");
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, appConfiguration);
            log.debug("Uploading updated configuration files ...");
            fileUploader.transferConfigFiles(deploymentId, descriptiveDeploymentId, configFileIdentifiers, configFileRepositoryRequired);
            if(configFileRepositoryRequired) {
                log.debug("Requesting configMap reload ...");
                janitorService.createOrReplaceConfigMap(descriptiveDeploymentId, domain);
            }
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATED);
        } catch(Exception e){
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) {
        try{
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVAL_INITIATED);
            this.fileUploader.removeConfigFiles(deploymentId);
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVED);
        } catch (Exception e){
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVAL_FAILED);
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
