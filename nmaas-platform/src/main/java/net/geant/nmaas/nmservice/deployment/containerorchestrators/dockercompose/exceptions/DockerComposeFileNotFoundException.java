package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions;

public class DockerComposeFileNotFoundException extends RuntimeException {

    public DockerComposeFileNotFoundException(String message) {
        super(message);
    }

    public DockerComposeFileNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
