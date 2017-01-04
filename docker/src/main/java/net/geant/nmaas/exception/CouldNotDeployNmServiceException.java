package net.geant.nmaas.exception;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotDeployNmServiceException extends Exception {

    public CouldNotDeployNmServiceException(String message) {
        super(message);
    }

    public CouldNotDeployNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
