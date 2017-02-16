package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNotFoundException extends Exception {

    public ContainerNotFoundException(String message) {
        super(message);
    }

    public ContainerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}