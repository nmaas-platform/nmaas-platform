package net.geant.nmaas.portal.exceptions;

public class ConfigurationTemplateParsingException extends RuntimeException {

    public ConfigurationTemplateParsingException(String message) {
        super(message);
    }

    public ConfigurationTemplateParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
