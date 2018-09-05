package net.geant.nmaas.externalservices.monitor.exceptions;

public class MonitorServiceNotFound extends RuntimeException {
    public MonitorServiceNotFound(String message){
        super(message);
    }
}
