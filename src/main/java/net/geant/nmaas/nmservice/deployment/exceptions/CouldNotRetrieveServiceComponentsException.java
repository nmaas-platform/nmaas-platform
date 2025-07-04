package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveServiceComponentsException extends RuntimeException {

    public CouldNotRetrieveServiceComponentsException(String message) {
        super(message);
    }

    public CouldNotRetrieveServiceComponentsException(String message, Throwable cause) {
        super(message, cause);
    }

}
