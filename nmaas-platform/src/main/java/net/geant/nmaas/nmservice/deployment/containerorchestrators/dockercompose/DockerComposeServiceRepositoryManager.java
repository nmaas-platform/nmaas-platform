package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerNmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerServiceRepositoryManager;
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
@Profile("env_docker-compose")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DockerComposeServiceRepositoryManager extends DockerServiceRepositoryManager<DockerComposeNmServiceInfo> implements DockerNmServiceRepositoryManager {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerComposeService(Identifier deploymentId, DockerComposeService dockerComposeService) throws InvalidDeploymentIdException {
        DockerComposeNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setDockerComposeService(dockerComposeService);
        repository.save(nmServiceInfo);
    }

    @Override
    public String loadAttachedVolumeName(Identifier deploymentId) throws InvalidDeploymentIdException {
        DockerComposeService dockerComposeService = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getDockerComposeService();
        if (dockerComposeService == null)
            throw new InvalidDeploymentIdException("Docker compose service is missing for deployment with id " + deploymentId);
        return dockerComposeService.getAttachedVolumeName();
    }

}
