package net.geant.nmaas.portal.exceptions;

import net.geant.nmaas.portal.api.exception.MarketException;

public class UndergoingMaintenanceException extends MarketException {
    public UndergoingMaintenanceException() {
        super();
    }
    public UndergoingMaintenanceException(String message) {
        super(message);
    }
}
