package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KServiceManipulationException extends Exception {

    public KServiceManipulationException(String message) {
        super(message);
    }

    public KServiceManipulationException(String message, Exception e) {
        super(message, e);
    }
}
