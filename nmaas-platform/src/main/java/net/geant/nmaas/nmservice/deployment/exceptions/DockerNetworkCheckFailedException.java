package net.geant.nmaas.nmservice.deployment.exceptions;

public class DockerNetworkCheckFailedException extends RuntimeException {

    public DockerNetworkCheckFailedException(String message) {
        super(message);
    }

}
