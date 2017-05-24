package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories.DockerNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerNetworkRepositoryManager {

    @Autowired
    private DockerNetworkRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeNetwork(DockerNetwork dockerNetwork) {
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNetwork(DockerNetwork dockerNetwork) {
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNetworkIdAndNetworkName(Identifier clientId, String networkId, String networkName) throws InvalidClientIdException {
        DockerNetwork dockerNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dockerNetwork.setDeploymentId(networkId);
        dockerNetwork.setDeploymentName(networkName);
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAddContainer(Identifier clientId, DockerContainer container) throws InvalidClientIdException {
        DockerNetwork dockerNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dockerNetwork.getDockerContainers().add(container);
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRemoveContainer(Identifier clientId, String containerId) throws InvalidClientIdException {
        DockerNetwork dockerNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        List<DockerContainer> updatedListOfContainers = dockerNetwork.getDockerContainers().stream().filter(c -> !c.getDeploymentId().equals(containerId)).collect(Collectors.toList());
        dockerNetwork.setDockerContainers(updatedListOfContainers);
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRemoveContainer(Identifier clientId, Long containerId) throws InvalidClientIdException {
        DockerNetwork dockerNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        List<DockerContainer> updatedListOfContainers = dockerNetwork.getDockerContainers().stream().filter(c -> !c.getId().equals(containerId)).collect(Collectors.toList());
        dockerNetwork.setDockerContainers(updatedListOfContainers);
        repository.save(dockerNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DockerNetwork loadNetwork(Identifier clientId) throws InvalidClientIdException {
        return repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeNetwork(Identifier clientId) throws InvalidClientIdException {
        DockerNetwork dockerNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        repository.delete(dockerNetwork.getId());
    }

    public boolean checkNetwork(Identifier clientId) {
        return repository.findByClientId(clientId).isPresent();
    }

}
