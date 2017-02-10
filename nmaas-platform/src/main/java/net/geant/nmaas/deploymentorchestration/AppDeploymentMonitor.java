package net.geant.nmaas.deploymentorchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentMonitor {

    AppDeploymentState deploymentState(Identifier deploymentId);

}
