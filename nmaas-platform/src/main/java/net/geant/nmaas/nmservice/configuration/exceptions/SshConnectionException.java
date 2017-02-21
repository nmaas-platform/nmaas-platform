package net.geant.nmaas.nmservice.configuration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SshConnectionException extends Exception {

    public SshConnectionException(String message) {
        super(message);
    }

    public SshConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
