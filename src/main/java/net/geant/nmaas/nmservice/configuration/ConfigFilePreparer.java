package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repositories.ConfigFileTemplatesRepository;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentParametersProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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
@Log4j2
class ConfigFilePreparer {

    private static final String DEFAULT_MANAGED_DEVICE_KEY = "targets";
    private static final String DEFAULT_MANAGED_DEVICE_IP_ADDRESS_KEY = "ipAddress";

    @Autowired
    private NmServiceConfigFileRepository configurations;

    @Autowired
    private ConfigFileTemplatesRepository templatesRepository;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    private AppDeploymentParametersProvider deploymentParametersProvider;

    List<String> generateAndStoreConfigFiles(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration) {
        log.debug(String.format("Generating configuration files for %s", deploymentId.value()));
        Map<String, Object> appConfigurationModel = new HashMap<>();
        log.debug("Adding default set of model parameters");
        appConfigurationModel.putAll(deploymentParametersProvider.deploymentParameters(deploymentId));
        log.debug("Adding user provided model parameters");
        appConfigurationModel.putAll(createModelEntriesFromUserInput(appConfiguration));
        log.debug("Adding bean model parameters");
        appConfigurationModel.put("helper", new ConfigFilePreparerHelper());
        updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, appConfigurationModel);
        List<String> configIds = new ArrayList<>();
        log.debug("Generating configuration files");
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
        log.debug(String.format("Returning configuration files identifiers %s", configIds));
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