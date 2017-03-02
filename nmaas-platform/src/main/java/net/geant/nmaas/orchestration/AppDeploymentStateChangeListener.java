package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;

/**
 * Declares methods to be called by {@link NmServiceDeploymentProvider}, {@link NmServiceConfigurationProvider}
 * and {@link DcnDeploymentProvider} components once certain deployment step is completed.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentStateChangeListener {

    /**
     * Notifies current state of the DCN deployment.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param state current state of the DCN deployment
     */
    void notifyStateChange(Identifier deploymentId, DcnDeploymentState state);

    /**
     * Notifies current state of the NM Service deployment.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param state current state of the NM Service deployment
     */
    void notifyStateChange(Identifier deploymentId, NmServiceDeploymentState state);

    /**
     * Notifies about deployment failure due to some generic error
     *
     * @param deploymentId unique identifier of the deployed user application
     */
    void notifyGenericError(Identifier deploymentId);

}