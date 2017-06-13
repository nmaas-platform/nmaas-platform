package net.geant.nmaas.dcn.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotPrepareDcnException extends Exception {

    public CouldNotPrepareDcnException(String message) {
        super(message);
    }

    public CouldNotPrepareDcnException(String message, Throwable cause) {
        super(message, cause);
    }

}
