package net.geant.nmaas.kubernetes.remote.api.exceptions;

public class RemoteClusterValidationException extends RuntimeException {


    public RemoteClusterValidationException(String message) {
        super(message);
    }

    public RemoteClusterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
