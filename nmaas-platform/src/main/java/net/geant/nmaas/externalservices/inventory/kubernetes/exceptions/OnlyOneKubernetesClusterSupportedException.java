package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class OnlyOneKubernetesClusterSupportedException extends Exception {

    public OnlyOneKubernetesClusterSupportedException(String message) {
        super(message);
    }

}
