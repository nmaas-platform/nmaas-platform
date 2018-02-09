package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerEngineNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("env_docker-engine")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DockerEngineServiceRepositoryManager extends DockerServiceRepositoryManager<DockerEngineNmServiceInfo> implements DockerNmServiceRepositoryManager {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainer(Identifier deploymentId, DockerContainer dockerContainer) throws InvalidDeploymentIdException {
        DockerEngineNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setDockerContainer(dockerContainer);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerNetworkDetails(Identifier deploymentId, DockerContainerNetDetails netDetails) throws InvalidDeploymentIdException {
        DockerEngineNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setNetworkDetails(netDetails);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerDeploymentId(Identifier deploymentId, String containerId) throws InvalidDeploymentIdException {
        DockerEngineNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setDeploymentId(containerId);
        repository.save(nmServiceInfo);
    }

    @Override
    public String loadAttachedVolumeName(Identifier deploymentId) throws InvalidDeploymentIdException {
        DockerContainer dockerContainer = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getDockerContainer();
        if (dockerContainer == null)
            throw new InvalidDeploymentIdException("Docker container is missing for deployment with id " + deploymentId);
        return dockerContainer.getVolumesDetails().getAttachedVolumeName();
    }

}
