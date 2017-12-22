package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class KubernetesClusterNotFoundExceptionException extends Exception {

    public KubernetesClusterNotFoundExceptionException(String message) {
        super(message);
    }

}
