package net.geant.nmaas.exception;

import com.spotify.docker.client.exceptions.DockerTimeoutException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotConnectToOrchestratorException extends Exception {

    public CouldNotConnectToOrchestratorException(String message) {
        super(message);
    }

    public CouldNotConnectToOrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

}
