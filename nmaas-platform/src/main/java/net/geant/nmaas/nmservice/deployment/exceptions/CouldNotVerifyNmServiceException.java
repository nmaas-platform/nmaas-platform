package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotVerifyNmServiceException extends Exception {

    public CouldNotVerifyNmServiceException(String message) {
        super(message);
    }

    public CouldNotVerifyNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
