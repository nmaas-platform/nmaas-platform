package net.geant.nmaas.dcn.deployment.exceptions;

public class CouldNotRemoveDcnException extends RuntimeException {

    public CouldNotRemoveDcnException(String message) {
        super(message);
    }

    public CouldNotRemoveDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
