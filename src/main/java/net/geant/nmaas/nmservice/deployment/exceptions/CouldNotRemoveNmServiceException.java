package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRemoveNmServiceException extends RuntimeException {

    public CouldNotRemoveNmServiceException(String message) {
        super(message);
    }

    public CouldNotRemoveNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
