package net.geant.nmaas.servicedeployment.exceptions;

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
