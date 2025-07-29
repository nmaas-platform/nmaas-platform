package net.geant.nmaas.nmservice.configuration;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.geant.nmaas.janitor.JanitorService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_REMOVAL_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_REMOVAL_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_REMOVED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_UPDATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_UPDATE_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURATION_UPDATE_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.CONFIGURED;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
            if (nsd.isConfigFileRepositoryRequired()) {
                List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(
                        deploymentId,
                        nsd.getApplicationId(),
                        nsd.getAppConfiguration());
                configHandler.createUser(nsd.getOwnerUsername(), nsd.getOwnerEmail(), nsd.getOwnerName(), nsd.getOwnerSshKeys());
                configHandler.createRepository(deploymentId, nsd.getOwnerUsername());
                if ((configFileIdentifiers != null && !configFileIdentifiers.isEmpty()) || nsd.isConfigUpdateEnabled()) {
                    configHandler.commitConfigFiles(deploymentId, configFileIdentifiers);
                }
                janitorService.createOrReplaceConfigMap(
                        Optional.ofNullable(nsd.getRemoteCluster()).map(KCluster::getClusterConfigFile).orElse(null),
                        nsd.getDescriptiveDeploymentId(),
                        nsd.getDomainName());
            }
            notifyStateChangeListenersWithDelay(deploymentId, CONFIGURED, 1000);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void configureBasicAuth(NmServiceDeployment nsd, String basicAuthUsername, String basicAuthPassword) {
        if (isNotEmpty(basicAuthUsername) && isNotEmpty(basicAuthPassword)) {
            janitorService.createOrReplaceBasicAuth(
                    Optional.ofNullable(nsd.getRemoteCluster()).map(KCluster::getClusterConfigFile).orElse(null),
                    nsd.getDescriptiveDeploymentId(),
                    nsd.getDomainName(),
                    basicAuthUsername,
                    basicAuthPassword);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateNmService(NmServiceDeployment nsd) {
        Identifier deploymentId = nsd.getDeploymentId();
        try {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_INITIATED);
            List<String> configFileIdentifiers = filePreparer.generateAndStoreConfigFiles(deploymentId, nsd.getApplicationId(), nsd.getAppConfiguration());
            if (nsd.isConfigFileRepositoryRequired()) {
                configHandler.commitConfigFiles(deploymentId, configFileIdentifiers);
                janitorService.createOrReplaceConfigMap(
                        Optional.ofNullable(nsd.getRemoteCluster()).map(KCluster::getClusterConfigFile).orElse(null),
                        nsd.getDescriptiveDeploymentId(),
                        nsd.getDomainName());
            }
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_UPDATE_FAILED, e.getMessage());
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void reloadNmService(NmServiceDeployment nsd) {
        try {
            notifyStateChangeListeners(nsd.getDeploymentId(), CONFIGURATION_UPDATE_INITIATED);
            janitorService.createOrReplaceConfigMap(
                    Optional.ofNullable(nsd.getRemoteCluster()).map(KCluster::getClusterConfigFile).orElse(null),
                    nsd.getDescriptiveDeploymentId(),
                    nsd.getDomainName());
            notifyStateChangeListeners(nsd.getDeploymentId(), CONFIGURATION_UPDATED);
        } catch (Exception e) {
            notifyStateChangeListeners(nsd.getDeploymentId(), CONFIGURATION_UPDATE_FAILED, e.getMessage());
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
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, CONFIGURATION_REMOVAL_FAILED);
            throw new NmServiceConfigurationFailedException(e.getMessage());
        }
    }

    @Override
    public AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId) {
        return configHandler.configRepositoryAccessDetails(deploymentId);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, ServiceDeploymentState state) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, ""));
    }

    @SneakyThrows
    private void notifyStateChangeListenersWithDelay(Identifier deploymentId, ServiceDeploymentState state, int delayInMilis) {
        Thread.sleep(delayInMilis);
        notifyStateChangeListeners(deploymentId, state);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, ServiceDeploymentState state, String errorMessage) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, errorMessage));
    }

}
