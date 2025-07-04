package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotScaleDownServiceException extends RuntimeException {

    public CouldNotScaleDownServiceException(String message) {
        super(message);
    }
}
