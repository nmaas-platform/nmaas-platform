package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRemoveContainerNetworkException extends RuntimeException {

    public CouldNotRemoveContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotRemoveContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
