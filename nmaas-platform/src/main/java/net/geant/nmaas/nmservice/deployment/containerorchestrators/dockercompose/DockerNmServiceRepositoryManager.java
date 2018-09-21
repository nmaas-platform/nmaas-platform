package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

public interface DockerNmServiceRepositoryManager {

    DockerHost loadDockerHost(Identifier deploymentId) throws InvalidDeploymentIdException;

    String loadAttachedVolumeName(Identifier deploymentId) throws InvalidDeploymentIdException;

}
