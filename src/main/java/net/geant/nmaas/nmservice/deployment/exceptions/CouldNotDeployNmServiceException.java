package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotDeployNmServiceException extends RuntimeException {

    public CouldNotDeployNmServiceException(String message) {
        super(message);
    }

    public CouldNotDeployNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
