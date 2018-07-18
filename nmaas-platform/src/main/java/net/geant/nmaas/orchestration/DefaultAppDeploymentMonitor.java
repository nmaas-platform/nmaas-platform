package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveNmServiceAccessDetailsException;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default {@link AppDeploymentMonitor} implementation.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor {

    @Autowired
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @Autowired
    private NmServiceDeploymentProvider serviceDeployment;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUiAccessDetails userAccessDetails(Identifier deploymentId) throws InvalidAppStateException, InvalidDeploymentIdException {
        if (AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED.equals(retrieveCurrentState(deploymentId)))
            return retrieveAccessDetails(deploymentId);
        else
            throw new InvalidAppStateException("Application deployment process didn't finish yet.");
    }

    @Override
    public List<AppDeploymentHistoryView> appDeploymentHistory(Identifier deploymentId) throws InvalidDeploymentIdException{
        return appDeploymentRepositoryManager.getAppStateHistoryByDeploymentId(deploymentId).stream()
                .map(value -> new AppDeploymentHistoryView(value.getTimestamp(), value.getPreviousState().toString(), value.getCurrentState().toString()))
                .collect(Collectors.toList());
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return appDeploymentRepositoryManager.loadState(deploymentId).lifecycleState();
    }

    private AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        try {
            return serviceDeployment.serviceAccessDetails(deploymentId);
        } catch (CouldNotRetrieveNmServiceAccessDetailsException e) {
            throw new InvalidDeploymentIdException(e.getMessage());
        }
    }

}
