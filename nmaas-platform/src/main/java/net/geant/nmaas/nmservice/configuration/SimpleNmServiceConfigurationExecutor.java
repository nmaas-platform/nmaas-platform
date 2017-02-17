package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            final List<Template> appConfigTemplates = templates.loadTemplates(applicationId);
            NmServiceConfiguration configuration;
            for(Template template : appConfigTemplates) {
                configuration = buildConfigFromTemplateAndUserProvidedInput(template);
                configurations.storeConfig(generateConfigId(), configuration);
            }
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED);
        }
    }

    private NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(Template template) throws Exception {
        ByteArrayOutputStream os  = new ByteArrayOutputStream();
        Writer osWriter = new OutputStreamWriter(os);
        template.process(null, osWriter);
        osWriter.flush();
        return new NmServiceConfiguration(configFileNameFromTemplateName(template.getName()), os.toByteArray());
    }

    String configFileNameFromTemplateName(String templateName) throws Exception {
        if (!templateName.endsWith(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX))
            throw new Exception("Invalid configuration template file name -> " + templateName);
        templateName = templateName.substring(templateName.lastIndexOf(File.separator) + 1);
        return templateName.replace(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX, "");
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        defaultAppDeploymentStateChangeListener.notifyStateChange(deploymentId, state);
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

    private String generateConfigId() {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.isConfigStored(generatedConfigId));
        return generatedConfigId;
    }
}
