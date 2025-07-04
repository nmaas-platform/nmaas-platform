package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveServiceAccessDetailsException extends RuntimeException {

    public CouldNotRetrieveServiceAccessDetailsException(String message) {
        super(message);
    }

    public CouldNotRetrieveServiceAccessDetailsException(String message, Throwable cause) {
        super(message, cause);
    }

}
