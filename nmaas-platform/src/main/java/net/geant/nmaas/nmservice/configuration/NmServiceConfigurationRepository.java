package net.geant.nmaas.nmservice.configuration;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class NmServiceConfigurationRepository {

    private Map<String, NmServiceConfiguration> configurations = new HashMap<>();

    public void storeConfig(String configId, NmServiceConfiguration config) {
        if(configId != null && config != null)
            configurations.put(configId, config);
    }

    public NmServiceConfiguration loadConfig(String configId) throws ConfigurationNotFoundException {
        NmServiceConfiguration requestedConfig = configurations.get(configId);
        if (requestedConfig != null)
            return requestedConfig;
        else
            throw new ConfigurationNotFoundException(
                    "Configuration " + configId + " not found in the repository.");
    }

    public boolean isConfigStored(String generatedConfigId) {
        return configurations.containsKey(generatedConfigId);
    }

    public class ConfigurationNotFoundException extends Exception {

        public ConfigurationNotFoundException(String message) {
            super(message);
        }

    }
}
