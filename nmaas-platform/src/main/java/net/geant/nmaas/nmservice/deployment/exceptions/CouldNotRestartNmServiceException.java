package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRestartNmServiceException extends RuntimeException {

    public CouldNotRestartNmServiceException(String message) {
        super(message);
    }

    public CouldNotRestartNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
