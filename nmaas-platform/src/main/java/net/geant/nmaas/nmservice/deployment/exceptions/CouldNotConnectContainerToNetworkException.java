package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotConnectContainerToNetworkException extends RuntimeException {

    public CouldNotConnectContainerToNetworkException(String message) {
        super(message);
    }

    public CouldNotConnectContainerToNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
