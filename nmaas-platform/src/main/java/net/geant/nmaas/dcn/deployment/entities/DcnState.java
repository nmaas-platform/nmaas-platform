package net.geant.nmaas.dcn.deployment.entities;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum DcnState {

    PROCESSED,
    DEPLOYED,
    REMOVED,
    NONE;

    public static DcnState fromDcnDeploymentState(DcnDeploymentState deploymentState) {
        switch (deploymentState) {
            case VERIFIED:
                return DEPLOYED;
            case REMOVED:
                return REMOVED;
            default:
                return PROCESSED;
        }
    }

}
