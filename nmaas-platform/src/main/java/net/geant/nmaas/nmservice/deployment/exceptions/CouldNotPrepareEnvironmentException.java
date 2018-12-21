package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotPrepareEnvironmentException extends RuntimeException {

    public CouldNotPrepareEnvironmentException(String message) {
        super(message);
    }

    public CouldNotPrepareEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
