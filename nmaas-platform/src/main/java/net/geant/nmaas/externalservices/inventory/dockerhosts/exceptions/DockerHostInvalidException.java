package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

public class DockerHostInvalidException extends RuntimeException {
    public DockerHostInvalidException(String message) {
        super(message);
    }
}
