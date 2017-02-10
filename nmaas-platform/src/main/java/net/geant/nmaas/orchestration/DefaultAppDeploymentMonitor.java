package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor, AppDeploymentStateChangeListener {

    @Autowired
    private AppLifecycleRepository repository;

    @Override
    public AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public void notifyStateChange(Identifier deploymentId, DcnDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    @Override
    public void notifyStateChange(Identifier deploymentId, NmServiceDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadCurrentState(deploymentId).lifecycleState();
    }

}
