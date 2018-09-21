package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions;

public class KServiceManipulationException extends RuntimeException {

    public KServiceManipulationException(String message) {
        super(message);
    }

    public KServiceManipulationException(String message, Exception e) {
        super(message, e);
    }
}
