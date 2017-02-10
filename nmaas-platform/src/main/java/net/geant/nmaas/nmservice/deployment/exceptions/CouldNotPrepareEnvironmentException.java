package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotPrepareEnvironmentException extends Exception {

    public CouldNotPrepareEnvironmentException(String message) {
        super(message);
    }

    public CouldNotPrepareEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
