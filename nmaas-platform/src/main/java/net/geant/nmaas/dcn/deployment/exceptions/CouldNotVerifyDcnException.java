package net.geant.nmaas.dcn.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotVerifyDcnException extends RuntimeException {

    public CouldNotVerifyDcnException(String message) {
        super(message);
    }

    public CouldNotVerifyDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
