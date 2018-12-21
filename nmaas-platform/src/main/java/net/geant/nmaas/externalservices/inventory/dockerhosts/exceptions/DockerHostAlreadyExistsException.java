package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

public class DockerHostAlreadyExistsException extends RuntimeException {
    public DockerHostAlreadyExistsException(String message) {
        super(message);
    }
}
