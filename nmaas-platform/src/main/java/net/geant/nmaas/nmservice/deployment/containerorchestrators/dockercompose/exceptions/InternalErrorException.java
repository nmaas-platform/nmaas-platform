package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InternalErrorException extends Exception {

    public InternalErrorException(String message) {
        super(message);
    }

    public InternalErrorException(String message, Exception e) {
        super(message, e);
    }
}
