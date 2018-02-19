package net.geant.nmaas.externalservices.inventory.kubernetes.exceptions;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class ExternalNetworkNotFoundException extends Exception {

    public ExternalNetworkNotFoundException(String message) {
        super(message);
    }

}