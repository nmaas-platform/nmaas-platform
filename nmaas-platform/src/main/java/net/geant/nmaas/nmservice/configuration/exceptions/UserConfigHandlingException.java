package net.geant.nmaas.nmservice.configuration.exceptions;

public class UserConfigHandlingException extends RuntimeException {

    public UserConfigHandlingException(String message) {
        super(message);
    }

    public UserConfigHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

}
