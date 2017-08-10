package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerNmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile({"docker-engine", "docker-compose"})
public class DockerNmServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Autowired
    private ConfigDownloadCommandExecutor configDownloadCommandExecutor;

    @Autowired
    private DockerNmServiceRepositoryManager serviceRepositoryManager;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration)
            throws NmServiceConfigurationFailedException {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configurationIdentifiers = configurationsPreparer.generateAndStoreConfigurations(deploymentId, applicationId, appConfiguration);
            DockerHost host = serviceRepositoryManager.loadDockerHost(deploymentId);
            DockerContainerVolumesDetails containerVolumesDetails = serviceRepositoryManager.loadDockerContainerVolumesDetails(deploymentId);
            for (String configId : configurationIdentifiers) {
                triggerConfigurationDownloadOnRemoteHost(configId, host, containerVolumesDetails.getAttachedVolumeName());
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (UserConfigHandlingException e) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Wasn't able to map json configuration to model map.");
        } catch (ConfigTemplateHandlingException configTemplateHandlingException) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Caught some exception during configuration template processing -> " + configTemplateHandlingException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Couldn't execute configuration download command on remote host -> " + commandExecutionException.getMessage());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
            throw new NmServiceConfigurationFailedException("Wrong deployment identifier -> " + invalidDeploymentIdException.getMessage());
        }
    }

    private void triggerConfigurationDownloadOnRemoteHost(String configId, DockerHost host, String targetDirectoryName)
            throws CommandExecutionException {
        configDownloadCommandExecutor.executeConfigDownloadCommand(configId, host, targetDirectoryName);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
