package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.janitor;

public class JanitorResponseException extends RuntimeException {
    public JanitorResponseException(String message) {
        super(message);
    }
}
