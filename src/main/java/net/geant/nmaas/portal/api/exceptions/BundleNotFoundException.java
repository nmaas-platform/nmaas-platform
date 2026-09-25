package net.geant.nmaas.portal.api.exceptions;

public class BundleNotFoundException extends RuntimeException {
    public BundleNotFoundException(Long id) {
        super("Bundle with id " + id + " not found");
    }
}
