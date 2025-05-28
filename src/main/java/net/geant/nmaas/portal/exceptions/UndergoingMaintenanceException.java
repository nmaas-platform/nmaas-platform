package net.geant.nmaas.portal.exceptions;

import net.geant.nmaas.portal.api.exceptions.MarketException;

public class UndergoingMaintenanceException extends MarketException {
    public UndergoingMaintenanceException() {
        super();
    }
    public UndergoingMaintenanceException(String message) {
        super(message);
    }
}
