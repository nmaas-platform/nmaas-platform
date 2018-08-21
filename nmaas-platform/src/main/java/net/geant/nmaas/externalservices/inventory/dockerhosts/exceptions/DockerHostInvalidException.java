package net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class DockerHostInvalidException extends RuntimeException {
    public DockerHostInvalidException(String message) {
        super(message);
    }
}
