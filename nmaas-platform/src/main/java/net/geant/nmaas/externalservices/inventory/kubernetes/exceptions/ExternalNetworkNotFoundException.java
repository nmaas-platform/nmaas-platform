package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

public class ExternalNetworkNotFoundException extends RuntimeException {

    public ExternalNetworkNotFoundException(String message) {
        super(message);
    }

}
