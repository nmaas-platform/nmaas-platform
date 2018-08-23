package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerNetworkCheckFailedException extends RuntimeException {

    public DockerNetworkCheckFailedException(String message) {
        super(message);
    }

}
