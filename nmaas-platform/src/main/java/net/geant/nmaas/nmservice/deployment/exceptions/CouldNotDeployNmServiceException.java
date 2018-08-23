package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotDeployNmServiceException extends RuntimeException {

    public CouldNotDeployNmServiceException(String message) {
        super(message);
    }

    public CouldNotDeployNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
