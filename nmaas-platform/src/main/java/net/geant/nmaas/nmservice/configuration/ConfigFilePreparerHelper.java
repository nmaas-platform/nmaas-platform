package net.geant.nmaas.nmservice.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigFilePreparerHelper {

    public String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    static Map<String, Object> createModelEntriesFromUserInput(AppConfiguration appConfiguration) {
        try {
            return new ObjectMapper().readValue(appConfiguration.getJsonInput(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new UserConfigHandlingException("Wasn't able to map json configuration to model map -> " + e.getMessage());
        }
    }

    static Template convertToFreemarkerTemplate(ConfigFileTemplate configFileTemplate) {
        try {
            return new Template(configFileTemplate.getConfigFileName(),
                    new StringReader(configFileTemplate.getConfigFileTemplateContent()),
                    new Configuration(Configuration.VERSION_2_3_28));
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException("Error during configuration file template processing -> " + e.getMessage());
        }
    }

    static String generateNewConfigId(List<NmServiceConfiguration> configurations) {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.stream()
                .map(NmServiceConfiguration::getConfigId)
                .collect(Collectors.toList())
                .contains(generatedConfigId)
        );
        return generatedConfigId;
    }

}
