package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

public class DockerComposeFileTemplateHandlingException extends RuntimeException {

    public DockerComposeFileTemplateHandlingException(String message) {
        super(message);
    }

    public DockerComposeFileTemplateHandlingException(String message, Exception e) {
        super(message, e);
    }
}
