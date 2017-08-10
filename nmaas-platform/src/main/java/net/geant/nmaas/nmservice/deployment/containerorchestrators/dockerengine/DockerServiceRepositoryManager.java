package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public abstract class DockerServiceRepositoryManager<T extends DockerNmServiceInfo> extends NmServiceRepositoryManager<T> implements DockerNmServiceRepositoryManager {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerHost(Identifier deploymentId, DockerHost host) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setHost(host);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainer(Identifier deploymentId, DockerContainer dockerContainer) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setDockerContainer(dockerContainer);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerNetworkDetails(Identifier deploymentId, DockerContainerNetDetails netDetails) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setNetworkDetails(netDetails);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerDeploymentId(Identifier deploymentId, String containerId) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setDeploymentId(containerId);
        repository.save(nmServiceInfo);
    }

    @Override
    public DockerHost loadDockerHost(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getHost();
    }

    @Override
    public DockerContainerVolumesDetails loadDockerContainerVolumesDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getDockerContainer().getVolumesDetails();
    }
}
