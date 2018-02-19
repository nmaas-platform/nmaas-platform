package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class IngressResourceManipulationException extends Exception {

    public IngressResourceManipulationException(String message) {
        super(message);
    }

    public IngressResourceManipulationException(String message, Exception e) {
        super(message, e);
    }
}
