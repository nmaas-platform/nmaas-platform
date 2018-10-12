package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;

public interface ConfigurationManager {

    Long addConfiguration(ConfigurationView configuration) throws OnlyOneConfigurationSupportedException;
    ConfigurationView getConfiguration();
    void updateConfiguration(Long id, ConfigurationView updatedConfiguration) throws ConfigurationNotFoundException;
    void deleteAllConfigurations();
}
