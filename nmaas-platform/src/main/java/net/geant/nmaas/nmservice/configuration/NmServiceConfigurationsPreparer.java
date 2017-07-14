package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
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

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
class NmServiceConfigurationsPreparer {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "targets";
    private static final String DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY = "ipAddress";

    @Autowired
    private NmServiceConfigurationRepository configurations;

    @Autowired
    private NmServiceConfigurationTemplatesRepository templates;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Loggable(LogLevel.DEBUG)
    List<String> generateAndStoreConfigurations(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration)
            throws ConfigTemplateHandlingException, UserConfigHandlingException, InvalidDeploymentIdException {
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

    private String generateNewConfigId(NmServiceConfigurationRepository configurations) {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.findByConfigId(generatedConfigId).isPresent());
        return generatedConfigId;
    }

    Template convertToTemplate(NmServiceConfigurationTemplate nmServiceConfigurationTemplate) throws ConfigTemplateHandlingException {
        try {
            return new Template(nmServiceConfigurationTemplate.getConfigFileName(),
                            new StringReader(nmServiceConfigurationTemplate.getConfigFileTemplateContent()),
                            new Configuration());
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException(e.getMessage());
        }
    }

    Map<String, Object> createModelFromJson(AppConfiguration appConfiguration) throws UserConfigHandlingException {
        try {
            return new ObjectMapper().readValue(appConfiguration.getJsonInput(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new UserConfigHandlingException(e.getMessage());
        }
    }

    private List<NmServiceConfigurationTemplate> loadConfigTemplatesForApplication(Identifier applicationId)
            throws ConfigTemplateHandlingException {
        List<NmServiceConfigurationTemplate> selectedTemplates = templates.findAllByApplicationId(applicationId.longValue());
        if (selectedTemplates.isEmpty())
            throw new ConfigTemplateHandlingException("No configuration templates found in repository for application with id " + applicationId);
        return selectedTemplates;
    }

    @Loggable(LogLevel.DEBUG)
    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel)
            throws InvalidDeploymentIdException {
        List<Object> devices = (List<Object>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY);
        List<String> ipAddresses = devices.stream()
                .map(device -> (String)((Map)device).get(DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY))
                .collect(Collectors.toList());
        nmServiceRepositoryManager.updateManagedDevices(deploymentId, ipAddresses);
    }

    private void storeConfigurationInRepository(NmServiceConfiguration configuration) {
        configurations.save(configuration);
    }

    NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(String configId, Template template, Object model)
            throws ConfigTemplateHandlingException {
        Writer stringWriter = new StringWriter();
        NmServiceConfiguration configuration = null;
        try {
            template.process(model, stringWriter);
            configuration = new NmServiceConfiguration(configId, template.getName(), stringWriter.toString());
        } catch (TemplateException e) {
            throw new ConfigTemplateHandlingException("Propagating TemplateException");
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException("Propagating IOException");
        }
        return configuration;
    }

}