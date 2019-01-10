package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

public class ConfigMapCreationException extends RuntimeException {
    public ConfigMapCreationException(String message) {
        super(message);
    }
}
