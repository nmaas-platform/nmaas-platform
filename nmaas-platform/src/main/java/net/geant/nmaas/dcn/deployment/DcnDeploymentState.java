package net.geant.nmaas.dcn.deployment;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum DcnDeploymentState {

    INIT,
    REQUEST_VERIFIED,
    REQUEST_VERIFICATION_FAILED,
    ENVIRONMENT_PREPARED,
    ENVIRONMENT_PREPARATION_FAILED,
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
