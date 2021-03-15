package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;

public interface ConfigurationManager {

    Long setConfiguration(ConfigurationView configuration);
    ConfigurationView getConfiguration();
    void updateConfiguration(Long id, ConfigurationView updatedConfiguration);
}
