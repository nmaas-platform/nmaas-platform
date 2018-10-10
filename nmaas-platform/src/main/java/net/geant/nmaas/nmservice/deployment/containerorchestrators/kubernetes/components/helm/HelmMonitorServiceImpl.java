package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Log4j2
public class HelmMonitorServiceImpl implements MonitorService {

    private MonitorManager monitorManager;

    private HelmCommandExecutor helmCommandExecutor;

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    @Autowired
    public void setHelmCommandExecutor(HelmCommandExecutor helmCommandExecutor){
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void checkStatus(){
        try{
            helmCommandExecutor.executeVersionCommand();
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
            log.info("Helm instance is running");
        } catch(CommandExecutionException | IllegalStateException e){
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
            log.info("Helm instance is not running -> " + e.getMessage());
        }
    }

    @Override
    public ServiceType getServiceType(){
        return ServiceType.HELM;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        this.checkStatus();
    }
}
