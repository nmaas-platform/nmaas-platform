package net.geant.nmaas.externalservices.inventory.dockerhosts;

public class DockerHostNotFoundException extends Exception {

    public DockerHostNotFoundException(String message) {
        super(message);
    }

}
