package net.geant.nmaas.dcn.deployment.exceptions;

public class CouldNotDeployDcnException extends RuntimeException {

    public CouldNotDeployDcnException(String message) {
        super(message);
    }

    public CouldNotDeployDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
