package net.geant.nmaas.servicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotCheckContainerNetworkException extends Exception {

    public CouldNotCheckContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotCheckContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
