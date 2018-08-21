package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFileTemplateNotFoundException extends RuntimeException {

    public DockerComposeFileTemplateNotFoundException(String message) {
        super(message);
    }

    public DockerComposeFileTemplateNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
