package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

public class IngressControllerManipulationException extends RuntimeException {

    public IngressControllerManipulationException(String message) {
        super(message);
    }

    public IngressControllerManipulationException(String message, Exception e) {
        super(message, e);
    }
}
