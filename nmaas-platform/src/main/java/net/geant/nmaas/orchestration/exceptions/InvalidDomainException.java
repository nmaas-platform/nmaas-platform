package net.geant.nmaas.orchestration.exceptions;

public class InvalidDomainException extends RuntimeException {

    public InvalidDomainException() {
        super();
    }

    public InvalidDomainException(String message) {
        super(message);
    }

}
