package net.geant.nmaas.servicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class OrchestratorInternalErrorException extends Exception {

    public OrchestratorInternalErrorException(String message) {
        super(message);
    }

    public OrchestratorInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
