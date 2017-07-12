package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;

import java.io.File;
import java.util.UUID;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
class SimpleNmServiceConfigurationHelper {

    static String generateConfigId(NmServiceConfigurationRepository configurations) {
        String generatedConfigId;
        do {
            generatedConfigId = UUID.randomUUID().toString();
        } while(configurations.findByConfigId(generatedConfigId).isPresent());
        return generatedConfigId;
    }

    static String configFileNameFromTemplateName(String templateName) throws ConfigTemplateHandlingException {
        if (!templateName.endsWith(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX))
            throw new ConfigTemplateHandlingException("Invalid configuration template file name -> " + templateName);
        templateName = templateName.substring(templateName.lastIndexOf(File.separator) + 1);
        return templateName.replace(NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX, "");
    }

}
