package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcndeployment.DcnDeploymentState;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceDeploymentState;

/**
 * Declares methods to be called by both NM Service and DCN deployment components once certain deployment step is
 * completed.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentStateChangeListener {

    void notifyStateChange(DcnDeploymentState state);

    void notifyStateChange(NmServiceDeploymentState state);

}
