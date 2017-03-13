package net.geant.nmaas.orchestration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InvalidApplicationIdException extends Exception {

    public InvalidApplicationIdException() {
        super();
    }

    public InvalidApplicationIdException(String message) {
        super(message);
    }

}
