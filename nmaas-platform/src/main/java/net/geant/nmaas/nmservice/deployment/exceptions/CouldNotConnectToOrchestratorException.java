package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotConnectToOrchestratorException extends RuntimeException {

    public CouldNotConnectToOrchestratorException(String message) {
        super(message);
    }

    public CouldNotConnectToOrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

}
