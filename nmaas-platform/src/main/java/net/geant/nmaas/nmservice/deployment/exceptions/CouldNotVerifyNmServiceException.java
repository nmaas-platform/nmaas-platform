package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotVerifyNmServiceException extends RuntimeException {

    public CouldNotVerifyNmServiceException(String message) {
        super(message);
    }

    public CouldNotVerifyNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
