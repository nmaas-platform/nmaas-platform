package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerCheckFailedException extends RuntimeException {

    public ContainerCheckFailedException(String message) {
        super(message);
    }

}
