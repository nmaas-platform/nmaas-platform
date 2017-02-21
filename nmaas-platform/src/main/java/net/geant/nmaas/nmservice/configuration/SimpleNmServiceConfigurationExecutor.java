package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.nmservice.configuration.SimpleNmServiceConfigurationHelper.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class SimpleNmServiceConfigurationExecutor implements NmServiceConfigurationProvider, AppDeploymentStateChanger {

    @Autowired
    private NmServiceConfigurationRepository configurations;

    @Autowired
    private NmServiceConfigurationTemplatesRepository templates;

    @Autowired
    private AppDeploymentStateChangeListener defaultAppDeploymentStateChangeListener;

    @Autowired
    private SshCommandExecutor sshCommandExecutor;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Override
    public void configureNmService(Identifier deploymentId, AppConfiguration appConfiguration, DockerHost host) {
        try {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
            final Identifier applicationId = appConfiguration.getApplicationId();
            for(Template template : loadConfigTemplatesForApplication(applicationId)) {
                generateConfigAndTriggerDownloadOnRemoteHost(deploymentId, host, template);
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            System.out.println("Failed to configure NM Service -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        }
    }

    void generateConfigAndTriggerDownloadOnRemoteHost(Identifier deploymentId, DockerHost host, Template template) throws Exception {
        String configId = generateConfigId(configurations);
        NmServiceConfiguration configuration = buildConfigFromTemplateAndUserProvidedInput(configId, template, oxidizedDefaultConfigurationInputModel());
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
