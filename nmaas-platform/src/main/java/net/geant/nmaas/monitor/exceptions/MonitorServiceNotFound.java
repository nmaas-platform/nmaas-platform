package net.geant.nmaas.monitor.exceptions;

public class MonitorServiceNotFound extends RuntimeException {
    public MonitorServiceNotFound(String message){
        super(message);
    }
}
