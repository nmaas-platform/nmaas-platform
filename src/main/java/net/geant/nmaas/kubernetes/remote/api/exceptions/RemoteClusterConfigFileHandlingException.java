package net.geant.nmaas.kubernetes.remote.api.exceptions;

public class RemoteClusterConfigFileHandlingException extends RuntimeException {

    public RemoteClusterConfigFileHandlingException(String message) {
        super(message);
    }

    public RemoteClusterConfigFileHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteClusterConfigFileHandlingException(Throwable cause) {
        super(cause);
    }
}
