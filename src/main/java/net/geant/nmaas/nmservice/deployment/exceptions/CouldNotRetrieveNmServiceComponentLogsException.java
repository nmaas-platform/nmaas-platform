package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveNmServiceComponentLogsException extends RuntimeException {

    public CouldNotRetrieveNmServiceComponentLogsException(String message) {
        super(message);
    }

    public CouldNotRetrieveNmServiceComponentLogsException(String message, Throwable cause) {
        super(message, cause);
    }

}
