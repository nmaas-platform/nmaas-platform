package net.geant.nmaas.nmservice.deployment.nmservice;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum NmServiceDeploymentState {
    INIT,
    REQUEST_VERIFIED,
    REQUEST_VERIFICATION_FAILED,
    ENVIRONMENT_PREPARATION_FAILED,
    READY_FOR_DEPLOYMENT,
    DEPLOYED,
    DEPLOYMENT_FAILED,
    VERIFIED,
    VERIFICATION_FAILED,
    READY_FOR_REMOVAL,
    REMOVAL_FAILED,
    REMOVED,
    UNKNOWN,
    ERROR;
}
