package net.geant.nmaas.nmservicedeployment.exceptions;

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
