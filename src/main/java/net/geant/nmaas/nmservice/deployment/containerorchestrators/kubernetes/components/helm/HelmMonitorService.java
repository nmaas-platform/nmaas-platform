package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.utils.bash.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelmMonitorService extends MonitorService {

    private final HelmCommandExecutor helmCommandExecutor;

    @Autowired
    public HelmMonitorService(MonitorManager monitorManager, HelmCommandExecutor helmCommandExecutor) {
        super(monitorManager);
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void checkStatus() {
        try {
            helmCommandExecutor.executeVersionCommand();
            this.updateMonitorEntry(MonitorStatus.SUCCESS);
            log.trace("Helm instance is running");
        } catch(CommandExecutionException | IllegalStateException e){
            this.updateMonitorEntry(MonitorStatus.FAILURE);
            log.error("Helm instance is not running -> {}", e.getMessage());
        }
    }

    @Override
    public ServiceType getServiceType(){
        return ServiceType.HELM;
    }

}
