package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class NmServiceRepositoryManager {

    @Autowired
    private NmServiceInfoRepository repository;

    @EventListener
    public void notifyStateChange(NmServiceDeploymentStateChangeEvent event) throws InvalidDeploymentIdException {
        updateServiceState(event.getDeploymentId(), event.getState());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateServiceState(Identifier deploymentId, NmServiceDeploymentState state) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setState(state);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerHost(Identifier deploymentId, DockerHost host) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setHost(host);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainer(Identifier deploymentId, DockerContainer dockerContainer) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setDockerContainer(dockerContainer);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerNetworkDetails(Identifier deploymentId, DockerContainerNetDetails netDetails) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setNetworkDetails(netDetails);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerContainerDeploymentId(Identifier deploymentId, String containerId) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.getDockerContainer().setDeploymentId(containerId);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateManagedDevices(Identifier deploymentId, List<String> ipAddresses) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setManagedDevicesIpAddresses(ipAddresses);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeService(NmServiceInfo serviceInfo) {
        repository.save(serviceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeService(Identifier deploymentId) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        repository.delete(nmServiceInfo.getId());
    }

    public NmServiceInfo loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public NmServiceDeploymentState loadCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getStateByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public Identifier loadClientId(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getClientIdByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

}
