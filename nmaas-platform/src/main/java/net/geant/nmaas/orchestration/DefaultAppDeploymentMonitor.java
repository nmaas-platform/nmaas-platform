package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveNmServiceAccessDetailsException;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default {@link AppDeploymentMonitor} implementation.
 */
@Component
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor {

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public DefaultAppDeploymentMonitor(AppDeploymentRepositoryManager appDeploymentRepositoryManager, NmServiceDeploymentProvider serviceDeployment){
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.serviceDeployment = serviceDeployment;
    }

    @Override
    public AppLifecycleState state(Identifier deploymentId) {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public AppLifecycleState previousState(Identifier deploymentId) {
        return retrievePreviousState(deploymentId);
    }

    @Override
    public List<AppDeployment> allDeployments() {
        return appDeploymentRepositoryManager.loadAll();
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUiAccessDetails userAccessDetails(Identifier deploymentId) {
        if (AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED.equals(retrieveCurrentState(deploymentId)))
            return retrieveAccessDetails(deploymentId);
        else
            throw new InvalidAppStateException("Application deployment process didn't finish yet.");
    }

    @Override
    public List<AppDeploymentHistoryView> appDeploymentHistory(Identifier deploymentId) {
        return appDeploymentRepositoryManager.getAppStateHistoryByDeploymentId(deploymentId).stream()
                .map(value -> new AppDeploymentHistoryView(value.getTimestamp(), value.getPreviousStateString(), value.getCurrentStateString()))
                .collect(Collectors.toList());
    }

    private AppLifecycleState retrievePreviousState(Identifier deploymentId) {
        Optional<AppDeploymentHistory> history = appDeploymentRepositoryManager.getAppStateHistoryByDeploymentId(deploymentId).stream()
                .max(Comparator.comparing(AppDeploymentHistory::getTimestamp));
        if(history.isPresent() && history.get().getPreviousState() != null){
            return history.get().getPreviousState().lifecycleState();
        }
        return AppLifecycleState.UNKNOWN;
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) {
        return appDeploymentRepositoryManager.loadState(deploymentId).lifecycleState();
    }

    private AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) {
        try {
            return serviceDeployment.serviceAccessDetails(deploymentId);
        } catch (CouldNotRetrieveNmServiceAccessDetailsException e) {
            throw new InvalidDeploymentIdException(e.getMessage());
        }
    }

}
