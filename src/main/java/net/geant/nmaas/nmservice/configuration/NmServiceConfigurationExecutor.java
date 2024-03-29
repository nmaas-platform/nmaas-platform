package net.geant.nmaas.nmservice.configuration;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
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
@RequiredArgsConstructor
public class NmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    private final ConfigFilePreparer filePreparer;
    private final GitConfigHandler configHandler;
    private final JanitorService janitorService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(NmServiceDeployment nsd) {
        Identifier deploymentId = nsd.getDeploymentId();
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, nsd.getApplicationId(), nsd.getAppConfiguration());
            if(nsd.isConfigFileRepositoryRequired()) {
                configHandler.createUser(nsd.getOwnerUsername(), nsd.getOwnerEmail(), nsd.getOwnerName(), nsd.getOwnerSshKeys());
                configHandler.createRepository(deploymentId, nsd.getOwnerUsername());
                if ((configFileIdentifiers != null && !configFileIdentifiers.isEmpty()) || nsd.isConfigUpdateEnabled()) {
                    configHandler.commitConfigFiles(deploymentId, configFileIdentifiers);
                }
                janitorService.createOrReplaceConfigMap(nsd.getDescriptiveDeploymentId(), nsd.getDomainName());
            }
            notifyStateChangeListeners(deploymentId, CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateNmService(NmServiceDeployment nmServiceDeployment) {
        Identifier deploymentId = nmServiceDeployment.getDeploymentId();
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, nmServiceDeployment.getApplicationId(), nmServiceDeployment.getAppConfiguration());
            if(nmServiceDeployment.isConfigFileRepositoryRequired()) {
                configHandler.commitConfigFiles(deploymentId, configFileIdentifiers);
                janitorService.createOrReplaceConfigMap(nmServiceDeployment.getDescriptiveDeploymentId(), nmServiceDeployment.getDomainName());
            }
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void reloadNmService(NmServiceDeployment nmServiceDeployment) {
        try {
            notifyStateChangeListeners(nmServiceDeployment.getDeploymentId(), CONFIGURATION_UPDATE_INITIATED);
            janitorService.createOrReplaceConfigMap(nmServiceDeployment.getDescriptiveDeploymentId(), nmServiceDeployment.getDomainName());
            notifyStateChangeListeners(nmServiceDeployment.getDeploymentId(), CONFIGURATION_UPDATED);
        } catch (Exception e) {
            notifyStateChangeListeners(nmServiceDeployment.getDeploymentId(), CONFIGURATION_UPDATE_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVAL_INITIATED);
            configHandler.removeConfigFiles(deploymentId);
            notifyStateChangeListenersWithDelay(deploymentId, CONFIGURATION_REMOVED, 1000);
        } catch (Exception e){
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVAL_FAILED);
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    public AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId) {
        return configHandler.configRepositoryAccessDetails(deploymentId);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, ""));
    }

    @SneakyThrows
    private void notifyStateChangeListenersWithDelay(Identifier deploymentId, NmServiceDeploymentState state, int delayInMilis) {
        Thread.sleep(delayInMilis);
        notifyStateChangeListeners(deploymentId, state);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state, String errorMessage) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, errorMessage));
    }

}
