package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFileTemplateHandlingException extends RuntimeException {

    public DockerComposeFileTemplateHandlingException(String message) {
        super(message);
    }

    public DockerComposeFileTemplateHandlingException(String message, Exception e) {
        super(message, e);
    }
}
