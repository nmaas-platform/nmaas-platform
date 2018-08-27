package net.geant.nmaas.dcn.deployment.exceptions;

public class CouldNotPrepareDcnException extends RuntimeException {

    public CouldNotPrepareDcnException(String message) {
        super(message);
    }

    public CouldNotPrepareDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
