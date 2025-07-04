package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRemoveServiceException extends RuntimeException {

    public CouldNotRemoveServiceException(String message) {
        super(message);
    }

    public CouldNotRemoveServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
