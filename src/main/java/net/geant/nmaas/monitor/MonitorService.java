package net.geant.nmaas.monitor;

import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MonitorService implements Job {

    private MonitorManager monitorManager;

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager){
        this.monitorManager = monitorManager;
    }

    public abstract void checkStatus();

    public abstract ServiceType getServiceType();

    protected void updateMonitorEntry(MonitorStatus status) {
        this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), status);
    }

    protected boolean schedulable() {
        return true;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }
}
