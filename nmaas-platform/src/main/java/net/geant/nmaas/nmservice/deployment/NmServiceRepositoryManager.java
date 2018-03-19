package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public abstract class NmServiceRepositoryManager<T extends NmServiceInfo> {

    @Autowired
    protected NmServiceInfoRepository<T> repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeService(T serviceInfo) {
        repository.save(serviceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeService(Identifier deploymentId) throws InvalidDeploymentIdException {
        NmServiceInfo nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        repository.delete(nmServiceInfo.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeAllServices() {
        repository.deleteAll();
    }

    public T loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    @EventListener
    public void notifyStateChange(NmServiceDeploymentStateChangeEvent event) throws InvalidDeploymentIdException {
        updateServiceState(event.getDeploymentId(), event.getState());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateServiceState(Identifier deploymentId, NmServiceDeploymentState state) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setState(state);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateManagedDevices(Identifier deploymentId, List<String> ipAddresses) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setManagedDevicesIpAddresses(ipAddresses);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateGitLabProject(Identifier deploymentId, GitLabProject gitLabProject) throws InvalidDeploymentIdException {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setGitLabProject(gitLabProject);
        repository.save(nmServiceInfo);
    }

    public NmServiceDeploymentState loadCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getStateByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public String loadDomain(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getDomainByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public Identifier loadApplicationId(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getApplicationIdByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

}
