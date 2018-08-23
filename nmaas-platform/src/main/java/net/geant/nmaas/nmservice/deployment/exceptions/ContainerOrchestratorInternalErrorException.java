package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerOrchestratorInternalErrorException extends RuntimeException {

    public ContainerOrchestratorInternalErrorException(String message) {
        super(message);
    }

    public ContainerOrchestratorInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
