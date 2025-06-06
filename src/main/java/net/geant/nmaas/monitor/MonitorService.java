package net.geant.nmaas.monitor;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public abstract class MonitorService implements Job {

    private final MonitorManager monitorManager;

    public MonitorService(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    public abstract void checkStatus();

    public abstract ServiceType getServiceType();

    protected void updateMonitorEntry(MonitorStatus status) {
        this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), status);
    }

    public boolean schedulable() {
        return true;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }
}
