package net.geant.nmaas.nmservice.deployment.entities;

/**
 * NM service deployment states.
 */
public enum NmServiceDeploymentState {

    INIT(true, false),
    REQUEST_VERIFIED(true, false),
    REQUEST_VERIFICATION_FAILED(false, false),
    ENVIRONMENT_PREPARATION_INITIATED(true, false),
    ENVIRONMENT_PREPARED(true, false),
    ENVIRONMENT_PREPARATION_FAILED(false, false),
    READY_FOR_DEPLOYMENT(true, false),
    CONFIGURATION_INITIATED(true, false),
    CONFIGURED(true, false),
    CONFIGURATION_FAILED(false, false),
    DEPLOYMENT_INITIATED(true, false),
    DEPLOYED(true,false),
    DEPLOYMENT_FAILED(false, false),
    VERIFICATION_INITIATED(true, false),
    VERIFIED(true, true),
    VERIFICATION_FAILED(false, false),
    CONFIGURATION_UPDATE_INITIATED(true, false),
    CONFIGURATION_UPDATED(true, false),
    CONFIGURATION_UPDATE_FAILED(true, true),
    CONFIGURATION_REMOVAL_INITIATED(true, false),
    CONFIGURATION_REMOVED(true, false),
    CONFIGURATION_REMOVAL_FAILED(false, false),
    REMOVAL_INITIATED(false, false),
    REMOVED(false, false),
    REMOVAL_FAILED(false, false),
    RESTART_INITIATED(true, false),
    RESTARTED(true, true),
    RESTART_FAILED(false, false),
    UPGRADE_INITIATED(true, false),
    UPGRADED(true, true),
    UPGRADE_FAILED(false, false),
    ERROR(false, false),
    FAILED_APPLICATION_REMOVED(false, false);

    private boolean isRunning;

    private boolean isOnline;

    NmServiceDeploymentState(boolean isRunning, boolean isOnline) {
        this.isRunning = isRunning;
        this.isOnline = isOnline;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isOnline() {
        return  isOnline;
    }

}
