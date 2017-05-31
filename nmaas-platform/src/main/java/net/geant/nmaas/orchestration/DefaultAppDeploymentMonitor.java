package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.AppDeployment;
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

import java.util.List;

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

    @Override
    public AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public List<AppDeployment> allDeployments() {
        return appDeploymentRepositoryManager.loadAll();
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
        return accessDetails(nmServiceRepositoryManager.loadService(deploymentId));
    }

    AppUiAccessDetails accessDetails(NmServiceInfo serviceInfo) {
        final String accessAddress = serviceInfo.getHost().getPublicIpAddress().getHostAddress();
        final Integer accessPort = serviceInfo.getDockerContainer().getNetworkDetails().getPublicPort();
        return new AppUiAccessDetails(new StringBuilder().append("http://").append(accessAddress).append(":").append(accessPort).toString());
    }

}
