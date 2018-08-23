package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotRemoveContainerNetworkException extends RuntimeException {

    public CouldNotRemoveContainerNetworkException(String message) {
        super(message);
    }

    public CouldNotRemoveContainerNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
