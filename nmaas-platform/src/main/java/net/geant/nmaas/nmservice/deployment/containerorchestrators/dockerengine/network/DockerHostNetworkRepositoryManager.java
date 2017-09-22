package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerHostNetworkRepositoryManager {

    @Autowired
    private DockerHostNetworkRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeNetwork(DockerHostNetwork dockerHostNetwork) {
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNetwork(DockerHostNetwork dockerHostNetwork) {
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNetworkIdAndNetworkName(Identifier clientId, String networkId, String networkName) throws InvalidClientIdException {
        DockerHostNetwork dockerHostNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dockerHostNetwork.setDeploymentId(networkId);
        dockerHostNetwork.setDeploymentName(networkName);
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAssignedAddresses(Identifier clientId, List<String> assignedAddresses) throws InvalidClientIdException {
        DockerHostNetwork dockerHostNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dockerHostNetwork.setAssignedAddresses(assignedAddresses);
        repository.save(dockerHostNetwork);
    }

    public DockerHostNetwork loadNetwork(Identifier clientId) throws InvalidClientIdException {
        return repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeNetwork(Identifier clientId) throws InvalidClientIdException {
        DockerHostNetwork dockerHostNetwork = repository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        repository.delete(dockerHostNetwork.getId());
    }

    public boolean checkNetwork(Identifier clientId) {
        return repository.findByClientId(clientId).isPresent();
    }

}
