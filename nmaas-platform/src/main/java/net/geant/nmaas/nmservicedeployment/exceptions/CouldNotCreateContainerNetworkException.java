package net.geant.nmaas.nmservicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotCreateContainerNetworkException extends Exception {

    public CouldNotCreateContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotCreateContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
