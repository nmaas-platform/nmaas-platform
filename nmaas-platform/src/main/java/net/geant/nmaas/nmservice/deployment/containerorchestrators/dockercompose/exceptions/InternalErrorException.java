package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

public class InternalErrorException extends RuntimeException {

    public InternalErrorException(String message) {
        super(message);
    }

    public InternalErrorException(String message, Exception e) {
        super(message, e);
    }
}
