package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveNmServiceComponentsException extends RuntimeException {

    public CouldNotRetrieveNmServiceComponentsException(String message) {
        super(message);
    }

    public CouldNotRetrieveNmServiceComponentsException(String message, Throwable cause) {
        super(message, cause);
    }

}
