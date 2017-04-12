package net.geant.nmaas.dcn.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotDeployDcnException extends Exception {

    public CouldNotDeployDcnException(String message) {
        super(message);
    }

    public CouldNotDeployDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
