package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("env_docker-compose")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DockerComposeServiceRepositoryManager extends NmServiceRepositoryManager<DockerComposeNmServiceInfo> implements DockerNmServiceRepositoryManager {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerHost(Identifier deploymentId, DockerHost host) {
        DockerComposeNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setHost(host);
        repository.save(nmServiceInfo);
    }

    public DockerHost loadDockerHost(Identifier deploymentId) {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getHost();
    }

    public List<DockerComposeNmServiceInfo> loadAllRunningServicesInDomain(String domain) {
        return repository.findAllByDomain(domain).stream().filter(service -> service.getState().isRunning()).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerComposeService(Identifier deploymentId, DockerComposeService dockerComposeService) {
        DockerComposeNmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setDockerComposeService(dockerComposeService);
        repository.save(nmServiceInfo);
    }

    @Override
    public String loadAttachedVolumeName(Identifier deploymentId) {
        DockerComposeService dockerComposeService = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId)).getDockerComposeService();
        if (dockerComposeService == null)
            throw new InvalidDeploymentIdException("Docker compose service is missing for deployment with id " + deploymentId);
        return dockerComposeService.getAttachedVolumeName();
    }

}
