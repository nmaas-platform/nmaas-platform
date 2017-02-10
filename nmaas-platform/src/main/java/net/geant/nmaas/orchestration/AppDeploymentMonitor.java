package net.geant.nmaas.orchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentMonitor {

    AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException;

}
