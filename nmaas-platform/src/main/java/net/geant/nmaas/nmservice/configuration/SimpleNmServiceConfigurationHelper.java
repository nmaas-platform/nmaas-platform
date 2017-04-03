package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;

import java.io.File;
import java.util.UUID;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SimpleNmServiceConfigurationHelper {

    public static String generateConfigId(NmServiceConfigurationRepository configurations) {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.isConfigStored(generatedConfigId));
        return generatedConfigId;
    }

    public static String configFileNameFromTemplateName(String templateName) throws ConfigTemplateHandlingException {
        if (!templateName.endsWith(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX))
            throw new ConfigTemplateHandlingException("Invalid configuration template file name -> " + templateName);
        templateName = templateName.substring(templateName.lastIndexOf(File.separator) + 1);
        return templateName.replace(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX, "");
    }

}