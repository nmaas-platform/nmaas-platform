package net.geant.nmaas.externalservices.inventory.dockerhosts;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class DockerHostExistsException extends Exception {
    public DockerHostExistsException(String message) {
        super(message);
    }
}
