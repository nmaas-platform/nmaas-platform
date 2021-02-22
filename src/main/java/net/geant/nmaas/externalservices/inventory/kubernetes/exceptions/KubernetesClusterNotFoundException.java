package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

public class KubernetesClusterNotFoundException extends RuntimeException {

    public KubernetesClusterNotFoundException(String message) {
        super(message);
    }

}
