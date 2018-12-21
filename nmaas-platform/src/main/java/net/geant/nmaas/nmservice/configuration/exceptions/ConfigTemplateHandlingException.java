package net.geant.nmaas.nmservice.configuration.exceptions;

public class ConfigTemplateHandlingException extends RuntimeException {

    public ConfigTemplateHandlingException(String message) {
        super(message);
    }

    public ConfigTemplateHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

}
