package net.geant.nmaas.monitor.targets;

import net.geant.nmaas.gitlab.GitLabManager;
import net.geant.nmaas.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GitLabMonitorService extends MonitorService {

    private final GitLabManager gitLabManager;

    @Autowired
    public GitLabMonitorService(MonitorManager monitorManager, GitLabManager gitLabManager) {
        super(monitorManager);
        this.gitLabManager = gitLabManager;
    }

    @Override
    public void checkStatus() {
        try {
            this.gitLabManager.validateGitLabInstance();
            this.updateMonitorEntry(MonitorStatus.SUCCESS);
        } catch (GitLabInvalidConfigurationException | IllegalStateException e) {
            this.updateMonitorEntry(MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.GITLAB;
    }

}