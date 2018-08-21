package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

public class DockerHostStateNotFoundException extends RuntimeException {

    public DockerHostStateNotFoundException(String message) {
        super(message);
    }

}
