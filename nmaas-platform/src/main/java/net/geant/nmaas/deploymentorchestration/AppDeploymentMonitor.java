package net.geant.nmaas.deploymentorchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentMonitor {

    AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException;

}
