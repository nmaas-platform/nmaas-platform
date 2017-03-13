package net.geant.nmaas.orchestration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InvalidDeploymentIdException extends Exception {

    public InvalidDeploymentIdException() {
        super();
    }

    public InvalidDeploymentIdException(String message) {
        super(message);
    }

}
