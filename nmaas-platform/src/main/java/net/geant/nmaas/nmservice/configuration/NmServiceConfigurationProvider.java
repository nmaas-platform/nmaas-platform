package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Defines a method to be used to apply custom configuration NM service being deployed.
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
     * @param host assigned Docker Host on which NM service will be deployed
     * @param containerDetails assigned persistent volume information that will be used by the NM service for
     *                         configuration files and data storage
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration configuration, DockerHost host, DockerContainerVolumesDetails containerDetails)
            throws NmServiceConfigurationFailedException;

}
