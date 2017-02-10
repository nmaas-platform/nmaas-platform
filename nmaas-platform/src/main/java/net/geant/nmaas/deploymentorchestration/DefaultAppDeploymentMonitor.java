package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor, AppDeploymentStateChangeListener {

    @Override
    public AppDeploymentState deploymentState(Identifier deploymentId) {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public void notifyStateChange(Identifier deploymentId, DcnDeploymentState state) {

    }

    @Override
    public void notifyStateChange(Identifier deploymentId, NmServiceDeploymentState state) {

    }

    private void storeState(Identifier deploymentId, AppDeploymentState state) {

    }

    private AppDeploymentState retrieveCurrentState(Identifier deploymentId) {
        return AppDeploymentState.UNKNOWN;
    }

}
