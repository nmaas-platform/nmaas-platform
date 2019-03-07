package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

public class JanitorResponseException extends RuntimeException {
    public JanitorResponseException(String message) {
        super(message);
    }
}
