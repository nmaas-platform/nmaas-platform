package net.geant.nmaas.externalservices.monitor.exceptions;

public class MonitorEntryNotFound extends RuntimeException {
    public MonitorEntryNotFound(String message){
        super(message);
    }
}
