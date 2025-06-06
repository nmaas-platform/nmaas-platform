package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JanitorMonitorService extends MonitorService {

    private final JanitorService janitorService;

    @Autowired
    public JanitorMonitorService(MonitorManager monitorManager, JanitorService janitorService) {
        super(monitorManager);
        this.janitorService = janitorService;
    }

    @Override
    public void checkStatus() {
        if (this.janitorService.isJanitorAvailable()) {
            this.updateMonitorEntry(MonitorStatus.SUCCESS);
        } else {
            this.updateMonitorEntry(MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.JANITOR;
    }

}
