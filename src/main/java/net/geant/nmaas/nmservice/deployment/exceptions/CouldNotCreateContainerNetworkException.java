package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotCreateContainerNetworkException extends RuntimeException {

    public CouldNotCreateContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotCreateContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
