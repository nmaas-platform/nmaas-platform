package net.geant.nmaas.nmservice.deployment.exceptions;

public class ServiceCheckFailedException extends RuntimeException {

    public ServiceCheckFailedException(String message) {
        super(message);
    }

}
