package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotConnectToOrchestratorException extends RuntimeException {

    public CouldNotConnectToOrchestratorException(String message) {
        super(message);
    }

    public CouldNotConnectToOrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

}
