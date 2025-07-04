package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotScaleUpServiceException extends RuntimeException {

    public CouldNotScaleUpServiceException(String message) {
        super(message);
    }
}
