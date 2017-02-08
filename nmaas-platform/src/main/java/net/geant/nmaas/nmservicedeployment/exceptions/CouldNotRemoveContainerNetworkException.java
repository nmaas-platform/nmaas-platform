package net.geant.nmaas.nmservicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotRemoveContainerNetworkException extends Exception {

    public CouldNotRemoveContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotRemoveContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
