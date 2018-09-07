package net.geant.nmaas.externalservices.inventory.shibboleth.exceptions;

public class ShibbolethConfigNotFoundException extends RuntimeException {
    public ShibbolethConfigNotFoundException(String message){
        super(message);
    }
}
