package net.geant.nmaas.externalservices.gitlab;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.geant.nmaas.externalservices.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class GitLabMonitorService extends MonitorService {

    private GitLabManager gitLabManager;

    @Autowired
    public void setGitLabManager(GitLabManager gitLabManager) {
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