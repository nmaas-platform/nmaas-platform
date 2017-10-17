package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public abstract class DockerServiceRepositoryManager<T extends DockerNmServiceInfo> extends NmServiceRepositoryManager<T> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerHost(Identifier deploymentId, DockerHost host) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setHost(host);
        repository.save(nmServiceInfo);
    }

    public DockerHost loadDockerHost(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getHost();
    }

    public List<T> loadAllRunningClientServices(Identifier clientId) {
        return repository.findAllByClientId(clientId).stream().filter(service -> service.getState().isRunning()).collect(Collectors.toList());
    }

}
