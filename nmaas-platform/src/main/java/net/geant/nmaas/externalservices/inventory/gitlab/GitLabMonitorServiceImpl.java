package net.geant.nmaas.externalservices.inventory.gitlab;

import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.externalservices.monitor.MonitorManager;
import net.geant.nmaas.externalservices.monitor.MonitorService;
import net.geant.nmaas.externalservices.monitor.MonitorStatus;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@NoArgsConstructor
public class GitLabMonitorServiceImpl implements MonitorService {

    private GitLabManager gitLabManager;

    private MonitorManager monitorManager;

    @Autowired
    public void setGitLabManager(GitLabManager gitLabManager) {
        this.gitLabManager = gitLabManager;
    }

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager){
        this.monitorManager = monitorManager;
    }

    @Override
    public void checkStatus(){
        this.gitLabManager.validateGitLabInstance();
    }

    @Override
    public ServiceType getServiceType(){
        return ServiceType.GITLAB;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        try {
            this.checkStatus();
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
        } catch (GitLabInvalidConfigurationException e){
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
        }
    }
}
