package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NmServiceConfigurationsPreparer {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "routers";

    @Autowired
    private NmServiceConfigurationRepository configurations;

    @Autowired
    private NmServiceConfigurationTemplatesRepository templates;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    @Autowired
    private NmServiceRepository nmServices;

    public List<String> generateAndStoreConfigurations(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration)
            throws DeploymentIdToNmServiceNameMapper.EntryNotFoundException, NmServiceRepository.ServiceNotFoundException, ConfigTemplateHandlingException, IOException {
        final Map<String, Object> appConfigurationModel = getModelFromJson(appConfiguration);
        updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, appConfigurationModel);
        List<String> configurationsIdentifiers = new ArrayList<>();
        for (Template template : loadConfigTemplatesForApplication(applicationId)) {
            String configId = generateConfigId(configurations);
            storeConfigurationInRepository(configId, buildConfigFromTemplateAndUserProvidedInput(configId, template, appConfigurationModel));
            configurationsIdentifiers.add(configId);
        }
        return configurationsIdentifiers;
    }

    @Loggable(LogLevel.DEBUG)
    Map<String, Object> getModelFromJson(AppConfiguration appConfiguration) throws IOException {
        return new ObjectMapper().readValue(appConfiguration.getJsonInput(), Map.class);
    }

    private List<Template> loadConfigTemplatesForApplication(Identifier applicationId)
            throws ConfigTemplateHandlingException {
        return templates.loadTemplates(applicationId);
    }

    @Loggable(LogLevel.DEBUG)
    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel)
            throws DeploymentIdToNmServiceNameMapper.EntryNotFoundException, NmServiceRepository.ServiceNotFoundException {
        final String nmServiceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
        nmServices.updateManagedDevices(nmServiceName, (List<String>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY));
    }

    void storeConfigurationInRepository(String configId, NmServiceConfiguration configuration) {
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

}
