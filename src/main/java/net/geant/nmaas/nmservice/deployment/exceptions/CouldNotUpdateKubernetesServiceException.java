package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotUpdateKubernetesServiceException extends RuntimeException {

    public CouldNotUpdateKubernetesServiceException(String message) {
        super(message);
    }

    public CouldNotUpdateKubernetesServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
