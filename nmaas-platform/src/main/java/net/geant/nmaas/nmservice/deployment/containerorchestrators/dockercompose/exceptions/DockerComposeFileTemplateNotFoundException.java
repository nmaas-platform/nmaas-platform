package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

public class DockerComposeFileTemplateNotFoundException extends RuntimeException {

    public DockerComposeFileTemplateNotFoundException(String message) {
        super(message);
    }

    public DockerComposeFileTemplateNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
