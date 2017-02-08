package net.geant.nmaas.nmservicedeployment.nmservice;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum NmServiceDeploymentState {

    INIT,
    READY_FOR_DEPLOYMENT,
    DEPLOYED,
    READY_FOR_REMOVAL,
    REMOVED,
    UNKNOWN,
    ERROR;

}
