package net.geant.nmaas.nmservice.configuration.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceConfigurationFailedException extends Exception {

    public NmServiceConfigurationFailedException(String message) {
        super(message);
    }

    public NmServiceConfigurationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
