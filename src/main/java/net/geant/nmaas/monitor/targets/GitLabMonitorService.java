package net.geant.nmaas.monitor.targets;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.gitlab.GitLabManager;
import net.geant.nmaas.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitLabMonitorService extends MonitorService {

    private final GitLabManager gitLabManager;

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