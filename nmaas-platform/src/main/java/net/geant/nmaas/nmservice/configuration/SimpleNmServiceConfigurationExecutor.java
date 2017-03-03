package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.geant.nmaas.nmservice.configuration.SimpleNmServiceConfigurationHelper.configFileNameFromTemplateName;
import static net.geant.nmaas.nmservice.configuration.SimpleNmServiceConfigurationHelper.generateConfigId;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimpleNmServiceConfigurationExecutor implements NmServiceConfigurationProvider, AppDeploymentStateChanger {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "routers";

    @Autowired
    private NmServiceConfigurationRepository configurations;

    @Autowired
    private NmServiceConfigurationTemplatesRepository templates;

    @Autowired
    private AppDeploymentStateChangeListener defaultAppDeploymentStateChangeListener;

    @Autowired
    private SshCommandExecutor sshCommandExecutor;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    @Autowired
    private NmServiceRepository nmServices;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, AppConfiguration appConfiguration, DockerHost host) {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            final Identifier applicationId = appConfiguration.getApplicationId();
            final Map<String, Object> appConfigurationModel = getModelFromJson(appConfiguration);
            updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, appConfigurationModel);
            for (Template template : loadConfigTemplatesForApplication(applicationId)) {
                generateConfigAndTriggerDownloadOnRemoteHost(deploymentId, template, appConfigurationModel, host);
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            System.out.println("Failed to update configuration for already stored NM Service -> " + serviceNotFoundException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (IOException e) {
            System.out.println("Wasn't able to map json configuration to model map.");
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException entryNotFoundException) {
            System.out.println("Wrong deployment identifier -> " + entryNotFoundException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (ConfigTemplateHandlingException configTemplateHandlingException) {
            System.out.println("Caught some exception during configuration template processing -> " + configTemplateHandlingException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        } catch (CommandExecutionException commandExecutionException) {
            System.out.println("Couldn't execute configuration download command on remote host -> " + commandExecutionException.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        }
    }

    @Loggable(LogLevel.DEBUG)
    Map<String, Object> getModelFromJson(AppConfiguration appConfiguration) throws IOException {
        return new ObjectMapper().readValue(appConfiguration.getJsonInput(), Map.class);
    }

    @Loggable(LogLevel.DEBUG)
    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel)
            throws DeploymentIdToNmServiceNameMapper.EntryNotFoundException, NmServiceRepository.ServiceNotFoundException {
        final String nmServiceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
        nmServices.updateManagedDevices(nmServiceName, (List<String>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY));
    }

    @Loggable(LogLevel.DEBUG)
    void generateConfigAndTriggerDownloadOnRemoteHost(Identifier deploymentId, Template template, Map<String, Object> appConfigurationModel, DockerHost host)
            throws ConfigTemplateHandlingException, CommandExecutionException {
        String configId = generateConfigId(configurations);
        storeConfigurationInRepository(configId, buildConfigFromTemplateAndUserProvidedInput(configId, template, appConfigurationModel));
        triggerConfigurationDownloadOnRemoteHost(deploymentId, configId, host);
    }

    private List<Template> loadConfigTemplatesForApplication(Identifier applicationId)
            throws ConfigTemplateHandlingException {
        return templates.loadTemplates(applicationId);
    }

    private void triggerConfigurationDownloadOnRemoteHost(Identifier deploymentId, String configId, DockerHost host)
            throws CommandExecutionException {
        sshCommandExecutor.executeConfigDownloadCommand(deploymentId, configId, host);
    }

    private void storeConfigurationInRepository(String configId, NmServiceConfiguration configuration) {
        configurations.storeConfig(configId, configuration);
    }

    NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(String configId, Template template, Object model)
            throws ConfigTemplateHandlingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer osWriter = new OutputStreamWriter(os);
        NmServiceConfiguration configuration = null;
        try {
            template.process(model, osWriter);
            osWriter.flush();
            configuration = new NmServiceConfiguration(configId, configFileNameFromTemplateName(template.getName()), os.toByteArray());
        } catch (TemplateException e) {
            throw new ConfigTemplateHandlingException("Propagating TemplateException");
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException("Propagating IOException");
        }
        return configuration;
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        defaultAppDeploymentStateChangeListener.notifyStateChange(deploymentId, state);
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

}
