package net.geant.nmaas.dcn.deployment.entities;

public enum DcnState {

    PROCESSED,
    DEPLOYED,
    REMOVED,
    NONE;

    public static DcnState fromDcnDeploymentState(DcnDeploymentState deploymentState) {
        switch (deploymentState) {
            case INIT:
                return NONE;
            case VERIFIED:
                return DEPLOYED;
            case REMOVED:
                return REMOVED;
            default:
                return PROCESSED;
        }
    }

}
