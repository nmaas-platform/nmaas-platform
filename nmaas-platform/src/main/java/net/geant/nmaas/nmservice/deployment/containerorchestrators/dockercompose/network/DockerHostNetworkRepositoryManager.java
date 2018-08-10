package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network;

import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
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

    private DockerHostNetworkRepository repository;

    @Autowired
    public DockerHostNetworkRepositoryManager(DockerHostNetworkRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeNetwork(DockerHostNetwork dockerHostNetwork) {
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateNetwork(DockerHostNetwork dockerHostNetwork) {
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateNetworkIdAndNetworkName(String domain, String networkId, String networkName) throws InvalidDomainException {
        DockerHostNetwork dockerHostNetwork = repository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
        dockerHostNetwork.setDeploymentId(networkId);
        dockerHostNetwork.setDeploymentName(networkName);
        repository.save(dockerHostNetwork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateAssignedAddresses(String domain, List<String> assignedAddresses) throws InvalidDomainException {
        DockerHostNetwork dockerHostNetwork = repository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
        dockerHostNetwork.setAssignedAddresses(assignedAddresses);
        repository.save(dockerHostNetwork);
    }

    DockerHostNetwork loadNetwork(String domain) throws InvalidDomainException {
        return repository.findByDomain(domain)
                .orElseThrow(() -> new InvalidDomainException("No network found in repository for domain " + domain));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeNetwork(String domain) throws InvalidDomainException {
        DockerHostNetwork dockerHostNetwork = repository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
        repository.delete(dockerHostNetwork.getId());
    }

    public boolean checkNetwork(String domain) {
        return repository.findByDomain(domain).isPresent();
    }

}
