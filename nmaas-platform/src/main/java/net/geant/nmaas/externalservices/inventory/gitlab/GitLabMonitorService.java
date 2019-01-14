package net.geant.nmaas.externalservices.inventory.gitlab;

import lombok.NoArgsConstructor;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@NoArgsConstructor
public class GitLabMonitorService implements MonitorService {

    private GitLabManager gitLabManager;

    private MonitorManager monitorManager;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public void setGitLabManager(GitLabManager gitLabManager) {
        this.gitLabManager = gitLabManager;
    }

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher){this.eventPublisher = eventPublisher;}

    @Override
    public void checkStatus() {
        try {
            this.gitLabManager.validateGitLabInstance();
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
        } catch (GitLabInvalidConfigurationException | IllegalStateException e) {
            eventPublisher.publishEvent(new NotificationEvent(this, getMailAttributes()));
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.GITLAB;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }

    private MailAttributes getMailAttributes(){
        return MailAttributes.builder()
                .mailType(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)
                .otherAttribute(this.getServiceType().getName())
                .build();
    }
}