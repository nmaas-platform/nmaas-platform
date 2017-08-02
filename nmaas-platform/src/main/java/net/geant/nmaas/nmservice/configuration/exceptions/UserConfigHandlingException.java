package net.geant.nmaas.nmservice.configuration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class UserConfigHandlingException extends Exception {

    public UserConfigHandlingException(String message) {
        super(message);
    }

    public UserConfigHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

}
