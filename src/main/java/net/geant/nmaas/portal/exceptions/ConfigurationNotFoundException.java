package net.geant.nmaas.portal.exceptions;

public class ConfigurationNotFoundException extends RuntimeException {
    public ConfigurationNotFoundException(String message){
        super(message);
    }
}
