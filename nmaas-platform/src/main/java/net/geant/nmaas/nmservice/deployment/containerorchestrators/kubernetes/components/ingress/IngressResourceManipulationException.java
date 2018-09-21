package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

public class IngressResourceManipulationException extends RuntimeException {

    public IngressResourceManipulationException(String message) {
        super(message);
    }

    public IngressResourceManipulationException(String message, Exception e) {
        super(message, e);
    }
}
