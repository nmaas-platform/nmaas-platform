package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Log4j2
public class HelmMonitorService extends MonitorService {

    private HelmCommandExecutor helmCommandExecutor;

    @Autowired
    public void setHelmCommandExecutor(HelmCommandExecutor helmCommandExecutor){
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void checkStatus(){
        try{
            helmCommandExecutor.executeVersionCommand();
            this.updateMonitorEntry(MonitorStatus.SUCCESS);
            log.debug("Helm instance is running");
        } catch(CommandExecutionException | IllegalStateException e){
            this.updateMonitorEntry(MonitorStatus.FAILURE);
            log.error("Helm instance is not running -> " + e.getMessage());
        }
    }

    @Override
    public ServiceType getServiceType(){
        return ServiceType.HELM;
    }

}
