package net.geant.nmaas.nmservice.configuration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ConfigFileNotFoundException extends RuntimeException {

    public ConfigFileNotFoundException(String message) {
        super(message);
    }

}
