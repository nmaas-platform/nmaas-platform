package net.geant.nmaas.nmservice.deployment.entities;

/**
 * NM service deployment states.
 */
public enum NmServiceDeploymentState {

    INIT(true),
    REQUEST_VERIFIED(true),
    REQUEST_VERIFICATION_FAILED(false),
    ENVIRONMENT_PREPARATION_INITIATED(true),
    ENVIRONMENT_PREPARED(true),
    ENVIRONMENT_PREPARATION_FAILED(false),
    READY_FOR_DEPLOYMENT(true),
    CONFIGURATION_INITIATED(true),
    CONFIGURED(true),
    CONFIGURATION_FAILED(false),
    DEPLOYMENT_INITIATED(true),
    DEPLOYED(true),
    DEPLOYMENT_FAILED(false),
    VERIFICATION_INITIATED(true),
    VERIFIED(true),
    VERIFICATION_FAILED(false),
    REMOVAL_INITIATED(false),
    REMOVED(false),
    REMOVAL_FAILED(false),
    RESTART_INITIATED(true),
    RESTARTED(true),
    RESTART_FAILED(false),
    ERROR(false);

    private boolean isRunning;

    NmServiceDeploymentState(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning;
    }

}
