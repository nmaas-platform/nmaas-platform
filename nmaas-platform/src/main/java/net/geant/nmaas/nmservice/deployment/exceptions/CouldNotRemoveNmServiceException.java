package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotRemoveNmServiceException extends Exception {

    public CouldNotRemoveNmServiceException(String message) {
        super(message);
    }

    public CouldNotRemoveNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
