package net.geant.nmaas.dcn.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotRemoveDcnException extends Exception {

    public CouldNotRemoveDcnException(String message) {
        super(message);
    }

    public CouldNotRemoveDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
