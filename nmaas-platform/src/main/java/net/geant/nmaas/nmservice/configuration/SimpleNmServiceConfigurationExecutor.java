package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
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
            for(Template template : loadConfigTemplatesForApplication(applicationId)) {
                generateConfigAndTriggerDownloadOnRemoteHost(deploymentId, template, appConfigurationModel, host);
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            System.out.println("Failed to configure NM Service -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        }
    }

    Map<String, Object> getModelFromJson(AppConfiguration appConfiguration) throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(appConfiguration.getJsonInput(), Map.class);
    }

    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel) throws DeploymentIdToNmServiceNameMapper.EntryNotFoundException, NmServiceRepository.ServiceNotFoundException {
        final String nmServiceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
        nmServices.updateManagedDevices(nmServiceName, (List<String>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY));
    }

    void generateConfigAndTriggerDownloadOnRemoteHost(Identifier deploymentId, Template template, Map<String, Object> appConfigurationModel, DockerHost host) throws Exception {
        String configId = generateConfigId(configurations);
        NmServiceConfiguration configuration = buildConfigFromTemplateAndUserProvidedInput(configId, template, appConfigurationModel);
        storeConfigurationInRepository(configId, configuration);
        try {
            triggerConfigurationDownloadOnRemoteHost(deploymentId, configId, host);
        } catch (CommandExecutionException e) {
            System.out.println("Failed to execute command -> " + e.getMessage());
            throw new CommandExecutionException("Failed to execute command -> " + e.getMessage(), e);
        }
    }

    List<Template> loadConfigTemplatesForApplication(Identifier applicationId) throws NmServiceConfigurationFailedException {
        return templates.loadTemplates(applicationId);
    }

    void triggerConfigurationDownloadOnRemoteHost(Identifier deploymentId, String configId, DockerHost host) throws CommandExecutionException {
        sshCommandExecutor.executeConfigDownloadCommand(deploymentId, configId, host);
    }

    void storeConfigurationInRepository(String configId, NmServiceConfiguration configuration) {
        configurations.storeConfig(configId, configuration);
    }

    NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(String configId, Template template, Object model) throws Exception {
        ByteArrayOutputStream os  = new ByteArrayOutputStream();
        Writer osWriter = new OutputStreamWriter(os);
        template.process(model, osWriter);
        osWriter.flush();
        return new NmServiceConfiguration(configId, configFileNameFromTemplateName(template.getName()), os.toByteArray());
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
