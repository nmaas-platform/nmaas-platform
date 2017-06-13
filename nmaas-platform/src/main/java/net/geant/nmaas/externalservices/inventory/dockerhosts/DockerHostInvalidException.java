package net.geant.nmaas.externalservices.inventory.dockerhosts;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class DockerHostInvalidException extends Exception {
    public DockerHostInvalidException(String message) {
        super(message);
    }
}
