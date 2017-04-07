package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor {

    @Autowired
    private AppLifecycleRepository repository;

    @Override
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

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) {
        if (notRelevantDcnDeploymentStateChange(event.getState()))
            return;
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(event.getDeploymentId()).nextState(event.getState());
            repository.updateDeploymentState(event.getDeploymentId(), newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    boolean notRelevantDcnDeploymentStateChange(DcnDeploymentState state) {
        switch (state) {
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return true;
            default:
                return false;
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(NmServiceDeploymentStateChangeEvent event) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(event.getDeploymentId()).nextState(event.getState());
            repository.updateDeploymentState(event.getDeploymentId(), newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyGenericError(AppDeploymentErrorEvent event) {
        repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.GENERIC_ERROR);
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadCurrentState(deploymentId).lifecycleState();
    }

    private AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadAccessDetails(deploymentId);
    }

}
