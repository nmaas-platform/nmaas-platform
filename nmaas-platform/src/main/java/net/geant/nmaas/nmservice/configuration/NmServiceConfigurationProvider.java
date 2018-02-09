package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Defines a method to be used to apply custom configuration for NM service being deployed.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceConfigurationProvider {

    /**
     * Coordinates NM service configuration process.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param configuration requesting client specific configuration to be applied
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration configuration)
            throws NmServiceConfigurationFailedException;

}
