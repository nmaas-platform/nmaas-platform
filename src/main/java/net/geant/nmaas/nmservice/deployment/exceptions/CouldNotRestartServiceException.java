package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRestartServiceException extends RuntimeException {

    public CouldNotRestartServiceException(String message) {
        super(message);
    }

    public CouldNotRestartServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
