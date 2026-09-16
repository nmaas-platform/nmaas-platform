package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;

public interface ConfigurationManager {

    Long setConfiguration(ConfigurationDto configuration);

    ConfigurationDto getConfiguration();

    void updateConfiguration(Long id, ConfigurationDto updatedConfiguration);

}
