package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotRetrieveNmServiceAccessDetailsException extends RuntimeException {

    public CouldNotRetrieveNmServiceAccessDetailsException(String message) {
        super(message);
    }

    public CouldNotRetrieveNmServiceAccessDetailsException(String message, Throwable cause) {
        super(message, cause);
    }

}
