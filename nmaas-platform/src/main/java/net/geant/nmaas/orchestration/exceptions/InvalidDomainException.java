package net.geant.nmaas.orchestration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InvalidDomainException extends RuntimeException {

    public InvalidDomainException() {
        super();
    }

    public InvalidDomainException(String message) {
        super(message);
    }

}
