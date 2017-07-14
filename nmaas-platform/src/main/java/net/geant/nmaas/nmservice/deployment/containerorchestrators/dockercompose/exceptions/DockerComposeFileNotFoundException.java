package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFileNotFoundException extends Exception {

    public DockerComposeFileNotFoundException(String message) {
        super(message);
    }

    public DockerComposeFileNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
