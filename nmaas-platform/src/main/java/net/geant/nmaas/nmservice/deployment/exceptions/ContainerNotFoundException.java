package net.geant.nmaas.nmservice.deployment.exceptions;

public class ContainerNotFoundException extends RuntimeException {

    public ContainerNotFoundException(String message) {
        super(message);
    }

    public ContainerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
