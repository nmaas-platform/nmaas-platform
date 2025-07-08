package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotVerifyServiceException extends RuntimeException {

    public CouldNotVerifyServiceException(String message) {
        super(message);
    }

    public CouldNotVerifyServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
