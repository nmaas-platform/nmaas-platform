package net.geant.nmaas.externalservices.inventory.shibboleth.exceptions;

public class OnlyOneShibbolethConfigSupportedException extends RuntimeException {
    public OnlyOneShibbolethConfigSupportedException(String message){
        super(message);
    }
}
