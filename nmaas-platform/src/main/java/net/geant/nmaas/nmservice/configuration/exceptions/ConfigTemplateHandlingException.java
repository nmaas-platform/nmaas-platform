package net.geant.nmaas.nmservice.configuration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ConfigTemplateHandlingException extends RuntimeException {

    public ConfigTemplateHandlingException(String message) {
        super(message);
    }

    public ConfigTemplateHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

}
