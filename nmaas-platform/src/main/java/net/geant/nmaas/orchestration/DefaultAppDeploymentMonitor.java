package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor, AppDeploymentStateChangeListener {

    @Autowired
    private AppLifecycleRepository repository;

    @Override
    @Loggable(LogLevel.INFO)
    public AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public Map<Identifier, AppLifecycleState> allDeployments() {
        return repository.loadViewOfAllDeployments();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public AppUiAccessDetails userAccessDetails(Identifier deploymentId) throws InvalidAppStateException, InvalidDeploymentIdException {
        if (AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED.equals(retrieveCurrentState(deploymentId)))
            return retrieveAccessDetails(deploymentId);
        else
            throw new InvalidAppStateException("Application deployment process didn't finish yet.");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(Identifier deploymentId, DcnDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(Identifier deploymentId, NmServiceDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyGenericError(Identifier deploymentId) {
        repository.updateDeploymentState(deploymentId, AppDeploymentState.GENERIC_ERROR);
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadCurrentState(deploymentId).lifecycleState();
    }

    private AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadAccessDetails(deploymentId);
    }

}
