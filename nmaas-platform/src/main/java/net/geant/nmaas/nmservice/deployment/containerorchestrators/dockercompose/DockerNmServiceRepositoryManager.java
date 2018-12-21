package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;

public interface DockerNmServiceRepositoryManager {

    DockerHost loadDockerHost(Identifier deploymentId);

    String loadAttachedVolumeName(Identifier deploymentId);

}
