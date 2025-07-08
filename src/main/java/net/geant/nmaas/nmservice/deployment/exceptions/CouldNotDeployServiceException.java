package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotDeployServiceException extends RuntimeException {

    public CouldNotDeployServiceException(String message) {
        super(message);
    }

    public CouldNotDeployServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
