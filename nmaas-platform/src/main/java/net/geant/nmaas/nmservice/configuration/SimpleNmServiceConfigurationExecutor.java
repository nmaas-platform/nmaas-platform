package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimpleNmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    private final static Logger log = LogManager.getLogger(SimpleNmServiceConfigurationExecutor.class);

    private NmServiceConfigurationsPreparer configurationsPreparer;

    private ConfigDownloadCommandExecutor configDownloadCommandExecutor;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public SimpleNmServiceConfigurationExecutor(
            NmServiceConfigurationsPreparer configurationsPreparer,
            ConfigDownloadCommandExecutor configDownloadCommandExecutor,
            ApplicationEventPublisher applicationEventPublisher) {
        this.configurationsPreparer = configurationsPreparer;
        this.configDownloadCommandExecutor = configDownloadCommandExecutor;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration, DockerHost host, DockerContainerVolumesDetails containerDetails)
            throws NmServiceConfigurationFailedException {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configurationIdentifiers = configurationsPreparer.generateAndStoreConfigurations(deploymentId, applicationId, appConfiguration);
            for (String configId : configurationIdentifiers) {
                triggerConfigurationDownloadOnRemoteHost(configId, host, containerDetails.getAttachedVolumeName());
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (UserConfigHandlingException e) {
            log.error("Wasn't able to map json configuration to model map.");
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Wasn't able to map json configuration to model map.");
        } catch (ConfigTemplateHandlingException configTemplateHandlingException) {
            log.error("Caught some exception during configuration template processing -> " + configTemplateHandlingException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Caught some exception during configuration template processing -> " + configTemplateHandlingException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            log.error("Couldn't execute configuration download command on remote host -> " + commandExecutionException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Couldn't execute configuration download command on remote host -> " + commandExecutionException.getMessage());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            log.error("Wrong deployment identifier -> " + invalidDeploymentIdException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Wrong deployment identifier -> " + invalidDeploymentIdException.getMessage());
        }
    }

    @Loggable(LogLevel.DEBUG)
    void triggerConfigurationDownloadOnRemoteHost(String configId, DockerHost host, String targetDirectoryName)
            throws CommandExecutionException {
        configDownloadCommandExecutor.executeConfigDownloadCommand(configId, host, targetDirectoryName);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
