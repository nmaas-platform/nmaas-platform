package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.*;
import net.geant.nmaas.orchestration.events.*;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class AppDeploymentLifecycleStateKeeper {

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ApplicationEvent> updateDeploymentState(Identifier deploymentId, AppDeploymentState currentState) throws InvalidDeploymentIdException {
        AppDeployment appDeployment = appDeploymentRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException());
        appDeployment.setState(currentState);
        appDeploymentRepository.save(appDeployment);
        return triggerActionEventIfRequired(deploymentId, currentState);
    }

    private Optional<ApplicationEvent> triggerActionEventIfRequired(Identifier deploymentId, AppDeploymentState currentState) {
        switch (currentState) {
            case REQUEST_VALIDATED:
                return Optional.of(new AppPrepareEnvironmentActionEvent(this, deploymentId));
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
                return Optional.of(new AppDeployDcnActionEvent(this, deploymentId));
            case MANAGEMENT_VPN_CONFIGURED:
                return Optional.of(new AppVerifyDcnActionEvent(this, deploymentId));
            case APPLICATION_CONFIGURED:
                return Optional.of(new AppDeployServiceActionEvent(this, deploymentId));
            case APPLICATION_DEPLOYED:
                return Optional.of(new AppVerifyServiceActionEvent(this, deploymentId));
            default:
                return Optional.empty();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppDeploymentState loadCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return appDeploymentRepository.getStateByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException("Deployment with id " + deploymentId + " not found in the repository. "));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<Identifier, AppLifecycleState> loadViewOfAllDeployments() {
        Map<Identifier, AppLifecycleState> view = new HashMap<>();
        appDeploymentRepository.findAll().forEach(item -> view.put(item.getDeploymentId(), item.getState().lifecycleState()));
        return view;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUiAccessDetails loadAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        try {
            return accessDetails(nmServiceRepositoryManager.loadService(deploymentId));
        } catch (DockerHostNotFoundException e) {
            throw new InvalidDeploymentIdException("Deployment with id " + deploymentId + " not found in the repository. ");
        }
    }

    private AppUiAccessDetails accessDetails(NmServiceInfo serviceInfo) throws DockerHostNotFoundException {
        try {
            final DockerHost host = serviceInfo.getHost();
            final String accessAddress = host.getPublicIpAddress().getHostAddress();
            final Integer accessPort = dockerHostStateKeeper.getAssignedPort(serviceInfo.getHost().getName(), serviceInfo.getDockerContainer());
            return new AppUiAccessDetails(new StringBuilder().append("http://").append(accessAddress).append(":").append(accessPort).toString());
        } catch (DockerHostState.MappingNotFoundException e) {
            throw new DockerHostNotFoundException("Problem with loading access port -> " + e.getMessage());
        }
    }

}
