package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeTemplateHandlingException extends Exception {

    public DockerComposeTemplateHandlingException(String message) {
        super(message);
    }

    public DockerComposeTemplateHandlingException(String message, Exception e) {
        super(message, e);
    }
}
