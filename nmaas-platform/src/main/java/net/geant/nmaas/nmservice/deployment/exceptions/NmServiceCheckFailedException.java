package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceCheckFailedException extends RuntimeException {

    public NmServiceCheckFailedException(String message) {
        super(message);
    }

}
