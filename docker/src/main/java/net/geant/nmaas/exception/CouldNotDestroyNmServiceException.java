package net.geant.nmaas.exception;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotDestroyNmServiceException extends Exception {

    public CouldNotDestroyNmServiceException(String message) {
        super(message);
    }

    public CouldNotDestroyNmServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
