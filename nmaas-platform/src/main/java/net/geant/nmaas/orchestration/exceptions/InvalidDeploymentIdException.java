package net.geant.nmaas.orchestration.exceptions;

import net.geant.nmaas.orchestration.entities.Identifier;

public class InvalidDeploymentIdException extends RuntimeException {

    public InvalidDeploymentIdException() {
        super();
    }

    public InvalidDeploymentIdException(String message) {
        super(message);
    }

    public InvalidDeploymentIdException(Identifier deploymentId) {
        super(deploymentId.value());
    }

}
