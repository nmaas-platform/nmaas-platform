package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.Identifier;

/**
 * Defines a method to be used to apply custom configuration for NM service being deployed
 */
public interface NmServiceConfigurationProvider {

    /**
     * Coordinates NM service configuration process
     *
     * @param nmServiceDeployment contains all necessary information about the application instance being configured
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void configureNmService(NmServiceDeployment nmServiceDeployment);

    /**
     * Updates NM service configuration
     *
     * @param nmServiceDeployment contains all necessary information about the application instance being configured
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void updateNmService(NmServiceDeployment nmServiceDeployment);

    /**
     * Removes NM service configuration
     *
     * @param deploymentId unique identifier of service deployment
     */
    void removeNmService(Identifier deploymentId);
}
