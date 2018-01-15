package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class KubernetesClusterNotFoundException extends Exception {

    public KubernetesClusterNotFoundException(String message) {
        super(message);
    }

}
