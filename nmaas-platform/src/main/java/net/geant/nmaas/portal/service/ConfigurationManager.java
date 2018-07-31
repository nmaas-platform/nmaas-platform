package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;

public interface ConfigurationManager {

    void addConfiguration(Configuration configuration) throws OnlyOneConfigurationSupportedException;
    Configuration getConfiguration();
    void updateConfiguration(Long id, Configuration updatedConfiguration) throws ConfigurationNotFoundException;
}
