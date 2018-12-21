package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

public class OnlyOneKubernetesClusterSupportedException extends RuntimeException {

    public OnlyOneKubernetesClusterSupportedException(String message) {
        super(message);
    }

}
