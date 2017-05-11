package net.geant.nmaas.nmservice.deployment.entities;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum NmServiceDeploymentState {

    INIT,
    REQUEST_VERIFIED,
    REQUEST_VERIFICATION_FAILED,
    ENVIRONMENT_PREPARED,
    ENVIRONMENT_PREPARATION_FAILED,
    READY_FOR_DEPLOYMENT,
    CONFIGURATION_INITIATED,
    CONFIGURED,
    CONFIGURATION_FAILED,
    DEPLOYMENT_INITIATED,
    DEPLOYED,
    DEPLOYMENT_FAILED,
    VERIFIED,
    VERIFICATION_FAILED,
    REMOVED,
    REMOVAL_FAILED,
    UNKNOWN,
    ERROR;

}
