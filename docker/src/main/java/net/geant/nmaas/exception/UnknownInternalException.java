package net.geant.nmaas.exception;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class UnknownInternalException extends Exception {

    public UnknownInternalException(String message) {
        super(message);
    }

    public UnknownInternalException(String message, Throwable cause) {
        super(message, cause);
    }

}
