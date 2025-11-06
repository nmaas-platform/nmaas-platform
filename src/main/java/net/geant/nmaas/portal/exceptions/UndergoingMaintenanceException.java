package net.geant.nmaas.portal.exceptions;

import net.geant.nmaas.portal.api.exceptions.PortalException;

public class UndergoingMaintenanceException extends PortalException {
    public UndergoingMaintenanceException() {
        super();
    }
    public UndergoingMaintenanceException(String message) {
        super(message);
    }
}
