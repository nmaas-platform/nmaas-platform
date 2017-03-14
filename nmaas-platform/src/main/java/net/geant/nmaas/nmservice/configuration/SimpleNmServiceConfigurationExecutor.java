package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimpleNmServiceConfigurationExecutor implements NmServiceConfigurationProvider, AppDeploymentStateChanger {

    private final static Logger log = LogManager.getLogger(SimpleNmServiceConfigurationExecutor.class);

    private AppDeploymentStateChangeListener stateChangeListener;

    private NmServiceConfigurationsPreparer configurationsPreparer;

    private SshCommandExecutor sshCommandExecutor;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Autowired
    public SimpleNmServiceConfigurationExecutor(AppDeploymentStateChangeListener stateChangeListener, NmServiceConfigurationsPreparer configurationsPreparer, SshCommandExecutor sshCommandExecutor) {
        this.stateChangeListener = stateChangeListener;
        this.configurationsPreparer = configurationsPreparer;
        this.sshCommandExecutor = sshCommandExecutor;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, AppConfiguration appConfiguration, DockerHost host) {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            List<String> configurationIdentifiers = configurationsPreparer.generateAndStoreConfigurations(deploymentId, appConfiguration);
            for (String configId : configurationIdentifiers) {
                triggerConfigurationDownloadOnRemoteHost(deploymentId, configId, host);
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            log.error("Failed to update configuration for already stored NM Service -> " + serviceNotFoundException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (IOException e) {
            log.error("Wasn't able to map json configuration to model map.");
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException entryNotFoundException) {
            log.error("Wrong deployment identifier -> " + entryNotFoundException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (ConfigTemplateHandlingException configTemplateHandlingException) {
            log.error("Caught some exception during configuration template processing -> " + configTemplateHandlingException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (CommandExecutionException commandExecutionException) {
            log.error("Couldn't execute configuration download command on remote host -> " + commandExecutionException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        }
    }

    @Loggable(LogLevel.DEBUG)
    void triggerConfigurationDownloadOnRemoteHost(Identifier deploymentId, String configId, DockerHost host)
            throws CommandExecutionException {
        sshCommandExecutor.executeConfigDownloadCommand(deploymentId, configId, host);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        stateChangeListener.notifyStateChange(deploymentId, state);
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

}
