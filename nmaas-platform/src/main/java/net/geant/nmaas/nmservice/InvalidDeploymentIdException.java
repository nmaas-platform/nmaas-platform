package net.geant.nmaas.nmservice;

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
