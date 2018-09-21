package net.geant.nmaas.nmservice.configuration.exceptions;

public class NmServiceConfigurationFailedException extends RuntimeException {

    public NmServiceConfigurationFailedException(String message) {
        super(message);
    }

    public NmServiceConfigurationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
