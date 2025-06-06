package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JanitorMonitorService extends MonitorService {

    private final JanitorService janitorService;

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
