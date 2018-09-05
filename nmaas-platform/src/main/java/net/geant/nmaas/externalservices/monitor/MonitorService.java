package net.geant.nmaas.externalservices.monitor;

import org.quartz.Job;

public interface MonitorService extends Job {
    void checkStatus();
    ServiceType getServiceType();
}
