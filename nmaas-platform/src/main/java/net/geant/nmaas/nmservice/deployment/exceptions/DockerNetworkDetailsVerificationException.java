package net.geant.nmaas.nmservice.deployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerNetworkDetailsVerificationException extends RuntimeException {

    public DockerNetworkDetailsVerificationException(String message) {
        super(message);
    }

}
