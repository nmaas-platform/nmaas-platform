package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import java.util.Date;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JanitorMonitorService implements MonitorService {

    private JanitorService janitorService;

    private MonitorManager monitorManager;

    @Autowired
    public JanitorMonitorService(JanitorService janitorService, MonitorManager monitorManager){
        this.janitorService = janitorService;
        this.monitorManager = monitorManager;
    }

    @Override
    public void checkStatus() {
        if(this.janitorService.isJanitorAvailable()){
            this.monitorManager.updateMonitorEntry(new Date(), getServiceType(), MonitorStatus.SUCCESS);
        } else {
            this.monitorManager.updateMonitorEntry(new Date(), getServiceType(), MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.JANITOR;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }
}
