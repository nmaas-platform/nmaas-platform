package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
class NmServiceConfigurationFilePreparer {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "targets";
    private static final String DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY = "ipAddress";

    private NmServiceConfigFileRepository configurations;

    private NmServiceConfigFileTemplatesRepository templates;

    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    NmServiceConfigurationFilePreparer(NmServiceConfigFileRepository configurations, NmServiceConfigFileTemplatesRepository templates, NmServiceRepositoryManager nmServiceRepositoryManager){
        this.configurations = configurations;
        this.templates = templates;
        this.nmServiceRepositoryManager = nmServiceRepositoryManager;
    }

    List<String> generateAndStoreConfigFiles(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration) {
        final Map<String, Object> appConfigurationModel = createModelFromJson(appConfiguration);
        updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, appConfigurationModel);
        List<String> configIds = new ArrayList<>();
        for (NmServiceConfigurationTemplate nmServiceConfigurationTemplate : loadConfigTemplatesForApplication(applicationId)) {
            final String configId = generateNewConfigId(configurations);
            final Template template = convertToTemplate(nmServiceConfigurationTemplate);
            final NmServiceConfiguration config = buildConfigFromTemplateAndUserProvidedInput(configId, template, appConfigurationModel);
            storeConfigurationInRepository(config);
            configIds.add(configId);
        }
        return configIds;
    }

    private String generateNewConfigId(NmServiceConfigFileRepository configurations) {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.findByConfigId(generatedConfigId).isPresent());
        return generatedConfigId;
    }

    Template convertToTemplate(NmServiceConfigurationTemplate nmServiceConfigurationTemplate) {
        try {
            return new Template(nmServiceConfigurationTemplate.getConfigFileName(),
                            new StringReader(nmServiceConfigurationTemplate.getConfigFileTemplateContent()),
                            new Configuration());
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException("Caught some exception during configuration template processing -> " + e.getMessage());
        }
    }

    Map<String, Object> createModelFromJson(AppConfiguration appConfiguration) {
        try {
            return new ObjectMapper().readValue(appConfiguration.getJsonInput(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new UserConfigHandlingException("Wasn't able to map json configuration to model map -> " + e.getMessage());
        }
    }

    private List<NmServiceConfigurationTemplate> loadConfigTemplatesForApplication(Identifier applicationId) {
        return templates.findAllByApplicationId(applicationId.longValue());
    }

    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel) {
        List<Object> devices = (List<Object>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY);
        if (devices == null)
            return;
        List<String> ipAddresses = devices.stream()
                .map(device -> (String)((Map)device).get(DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY))
                .collect(Collectors.toList());
        nmServiceRepositoryManager.updateManagedDevices(deploymentId, ipAddresses);
    }

    private void storeConfigurationInRepository(NmServiceConfiguration configuration) {
        configurations.save(configuration);
    }

    NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(String configId, Template template, Object model) {
        Writer stringWriter = new StringWriter();
        NmServiceConfiguration configuration = null;
        try {
            template.process(model, stringWriter);
            configuration = new NmServiceConfiguration(configId, template.getName(), stringWriter.toString());
        } catch (TemplateException
                | IOException e) {
            throw new ConfigTemplateHandlingException("Caught some exception during configuration file building from template and user data -> " + e.getMessage());
        }
        return configuration;
    }

}