package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repositories.ConfigFileTemplatesRepository;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentParametersProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.geant.nmaas.nmservice.configuration.ConfigFilePreparerHelper.convertToFreemarkerTemplate;
import static net.geant.nmaas.nmservice.configuration.ConfigFilePreparerHelper.createModelEntriesFromUserInput;
import static net.geant.nmaas.nmservice.configuration.ConfigFilePreparerHelper.generateNewConfigId;

@Component
@NoArgsConstructor
@AllArgsConstructor
class ConfigFilePreparer {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "targets";
    private static final String DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY = "ipAddress";

    private NmServiceConfigFileRepository configurations;
    private ConfigFileTemplatesRepository templatesRepository;
    private NmServiceRepositoryManager nmServiceRepositoryManager;
    private AppDeploymentParametersProvider deploymentParametersProvider;

    List<String> generateAndStoreConfigFiles(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration) {
        Map<String, Object> appConfigurationModel = new HashMap<>();
        appConfigurationModel.putAll(deploymentParametersProvider.deploymentParameters(deploymentId));
        appConfigurationModel.putAll(createModelEntriesFromUserInput(appConfiguration));
        updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, appConfigurationModel);
        List<String> configIds = new ArrayList<>();
        for (ConfigFileTemplate configFileTemplate : templatesRepository.getAllByApplicationId(applicationId.longValue())) {
            final String configId = generateNewConfigId(configurations.findAll());
            final Template freemarkerTemplate = convertToFreemarkerTemplate(configFileTemplate);
            final NmServiceConfiguration config = buildConfigFromTemplateAndUserProvidedInput(
                    configId,
                    configFileTemplate.getConfigFileName(),
                    configFileTemplate.getConfigFileDirectory(),
                    freemarkerTemplate,
                    appConfigurationModel);
            storeConfigurationInRepository(config);
            configIds.add(configId);
        }
        return configIds;
    }

    NmServiceConfiguration buildConfigFromTemplateAndUserProvidedInput(String configId, String configFileName, String configFileDirectory, Template template, Object model) {
        try {
            Writer stringWriter = new StringWriter();
            template.process(model, stringWriter);
            return new NmServiceConfiguration(
                    configId,
                    configFileName,
                    configFileDirectory,
                    stringWriter.toString());
        } catch (TemplateException
                | IOException e) {
            throw new ConfigTemplateHandlingException("Caught some exception during configuration file building from template and user data -> " + e.getMessage());
        }
    }

    void updateStoredNmServiceInfoWithListOfManagedDevices(Identifier deploymentId, Map<String, Object> appConfigurationModel) {
        List<Object> devices = (List<Object>) appConfigurationModel.get(DEFAULT_MANAGED_DEVICE_KEY);
        if (devices == null) {
            return;
        }
        List<String> ipAddresses = devices.stream()
                .map(device -> (String)((Map)device).get(DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY))
                .collect(Collectors.toList());
        nmServiceRepositoryManager.updateManagedDevices(deploymentId, ipAddresses);
    }

    private void storeConfigurationInRepository(NmServiceConfiguration configuration) {
        configurations.save(configuration);
    }

}