package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;

/**
 * Defines a method to be used to apply custom configuration for NM service being deployed.
 */
public interface NmServiceConfigurationProvider {

    /**
     * Coordinates NM service configuration process.
     *
     * @param deploymentId unique identifier of service deployment
     * @param descriptiveDeploymentId human readable identifier of the deployment
     * @param applicationId identifier of the application / service
     * @param configuration requesting user specific configuration to be applied
     * @param domain logic nmaas domain to create configuration in
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     * @throws NmServiceConfigurationFailedException if NM service couldn't be configured for some reason
     */
    void configureNmService(Identifier deploymentId, Identifier descriptiveDeploymentId, Identifier applicationId, AppConfiguration configuration,
                            String domain, boolean configFileRepositoryRequired);

    /**
     * Updates NM service configuration
     *
     * @param deploymentId unique identifier of service deployment
     * @param descriptiveDeploymentId human readable identifier of the deployment
     * @param applicationId identifier of the application / service
     * @param appConfiguration requesting user specific configuration to be applied
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     */
    void updateNmService(Identifier deploymentId, Identifier descriptiveDeploymentId, Identifier applicationId, AppConfiguration appConfiguration,
                         String domain, boolean configFileRepositoryRequired);

    /**
     * Removes NM service configuration
     *
     * @param deploymentId unique identifier of service deployment
     */
    void removeNmService(Identifier deploymentId);
}
