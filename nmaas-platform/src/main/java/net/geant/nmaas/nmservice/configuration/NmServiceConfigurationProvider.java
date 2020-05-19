package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigRepositoryAccessDetailsNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
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
     * Reloads NM service configuration from repository
     *
     * @param nmServiceDeployment contains all necessary information about the application instance being configured
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void reloadNmService(NmServiceDeployment nmServiceDeployment);

    /**
     * Removes NM service configuration
     *
     * @param deploymentId unique identifier of service deployment
     */
    void removeNmService(Identifier deploymentId);

    /**
     * Retrieves deployed service repository access details to be presented to the client
     *
     * @param deploymentId unique identifier of service deployment
     * @return repository access details
     * @throws ConfigRepositoryAccessDetailsNotFoundException if repository details could not be found
     */
    AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId);

}
