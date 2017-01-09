package net.geant.nmaas.orchestrators.dockerswarm.service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ServiceSpecVerificationException extends Exception {

    public ServiceSpecVerificationException(String message) {
        super(message);
    }

}
