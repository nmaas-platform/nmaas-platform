package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor {

    @Autowired
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Override
    public AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public Map<Identifier, AppLifecycleState> allDeployments() {
        return loadViewOfAllDeployments();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Map<Identifier, AppLifecycleState> loadViewOfAllDeployments() {
        Map<Identifier, AppLifecycleState> view = new HashMap<>();
        appDeploymentRepositoryManager.loadAll().forEach(item -> view.put(item.getDeploymentId(), item.getState().lifecycleState()));
        return view;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public AppUiAccessDetails userAccessDetails(Identifier deploymentId) throws InvalidAppStateException, InvalidDeploymentIdException {
        if (AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED.equals(retrieveCurrentState(deploymentId)))
            return retrieveAccessDetails(deploymentId);
        else
            throw new InvalidAppStateException("Application deployment process didn't finish yet.");
    }

    AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return appDeploymentRepositoryManager.loadState(deploymentId).lifecycleState();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        try {
            return accessDetails(nmServiceRepositoryManager.loadService(deploymentId));
        } catch (DockerHostNotFoundException e) {
            throw new InvalidDeploymentIdException("Deployment with id " + deploymentId + " not found in the repository. ");
        }
    }

    AppUiAccessDetails accessDetails(NmServiceInfo serviceInfo) throws DockerHostNotFoundException {
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
