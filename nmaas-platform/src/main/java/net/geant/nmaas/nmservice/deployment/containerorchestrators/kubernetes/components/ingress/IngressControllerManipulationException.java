package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class IngressControllerManipulationException extends RuntimeException {

    public IngressControllerManipulationException(String message) {
        super(message);
    }

    public IngressControllerManipulationException(String message, Exception e) {
        super(message, e);
    }
}
