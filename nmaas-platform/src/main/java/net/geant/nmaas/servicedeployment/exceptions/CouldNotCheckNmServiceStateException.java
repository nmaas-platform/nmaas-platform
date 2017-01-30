package net.geant.nmaas.servicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotCheckNmServiceStateException extends Exception {

    public CouldNotCheckNmServiceStateException(String message) {
        super(message);
    }

    public CouldNotCheckNmServiceStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
