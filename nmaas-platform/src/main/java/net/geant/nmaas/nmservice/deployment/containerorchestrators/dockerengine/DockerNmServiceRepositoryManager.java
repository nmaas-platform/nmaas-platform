package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DockerNmServiceRepositoryManager {

    DockerHost loadDockerHost(Identifier deploymentId) throws InvalidDeploymentIdException;

    DockerContainerVolumesDetails loadDockerContainerVolumesDetails(Identifier deploymentId) throws InvalidDeploymentIdException;

}
