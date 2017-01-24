package net.geant.nmaas.servicedeployment.nmservice;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum NmServiceState {

    INIT,
    READY_FOR_DEPLOYMENT,
    DEPLOYED,
    READY_FOR_REMOVAL,
    REMOVED,
    UNKNOWN,
    ERROR;

}
