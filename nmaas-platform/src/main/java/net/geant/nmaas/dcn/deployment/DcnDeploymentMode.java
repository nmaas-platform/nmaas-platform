package net.geant.nmaas.dcn.deployment;

/**
 * Determines which DCN deployment mechanism should be used. It can be done fully automatic, manually by the network
 * operator in which case a suggested network equipment configuration is prepared to be provided to the operator or
 * no mechanism can be triggered at all.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum DcnDeploymentMode {

    AUTO("auto"),
    MANUAL("manual"),
    NONE("none");

    private String value;

    DcnDeploymentMode(String value) {
        this.value = value;
    }

}
