package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveNmServiceAccessDetailsException extends RuntimeException {

    public CouldNotRetrieveNmServiceAccessDetailsException(String message) {
        super(message);
    }

    public CouldNotRetrieveNmServiceAccessDetailsException(String message, Throwable cause) {
        super(message, cause);
    }

}
