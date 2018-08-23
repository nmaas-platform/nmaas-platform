package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class DockerHostAlreadyExistsException extends RuntimeException {
    public DockerHostAlreadyExistsException(String message) {
        super(message);
    }
}
