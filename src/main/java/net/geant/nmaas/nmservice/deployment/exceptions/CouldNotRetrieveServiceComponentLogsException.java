package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotRetrieveServiceComponentLogsException extends RuntimeException {

    public CouldNotRetrieveServiceComponentLogsException(String message) {
        super(message);
    }

    public CouldNotRetrieveServiceComponentLogsException(String message, Throwable cause) {
        super(message, cause);
    }

}
