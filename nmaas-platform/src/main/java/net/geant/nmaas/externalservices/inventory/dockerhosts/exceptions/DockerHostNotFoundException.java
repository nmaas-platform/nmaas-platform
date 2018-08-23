package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

public class DockerHostNotFoundException extends RuntimeException {

    public DockerHostNotFoundException(String message) {
        super(message);
    }

}
