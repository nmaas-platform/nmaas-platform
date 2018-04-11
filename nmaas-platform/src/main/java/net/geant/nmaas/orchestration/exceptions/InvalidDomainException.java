package net.geant.nmaas.orchestration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InvalidDomainException extends Exception {

    public InvalidDomainException() {
        super();
    }

    public InvalidDomainException(String message) {
        super(message);
    }

}
