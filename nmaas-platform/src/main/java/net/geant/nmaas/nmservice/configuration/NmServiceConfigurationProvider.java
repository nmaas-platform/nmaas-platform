package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceConfigurationProvider {

    void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration configuration, DockerHost host, DockerContainerVolumesDetails containerDetails) throws NmServiceConfigurationFailedException;

}
