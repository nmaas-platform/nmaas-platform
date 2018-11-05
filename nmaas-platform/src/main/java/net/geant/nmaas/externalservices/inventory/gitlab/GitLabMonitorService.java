package net.geant.nmaas.externalservices.inventory.gitlab;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.portal.api.model.FailureEmail;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@NoArgsConstructor
public class GitLabMonitorService implements MonitorService {

    private GitLabManager gitLabManager;

    private MonitorManager monitorManager;

    private NotificationService notificationService;

    private UserService userService;

    @Autowired
    public void setGitLabManager(GitLabManager gitLabManager) {
        this.gitLabManager = gitLabManager;
    }

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager){
        this.monitorManager = monitorManager;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void checkStatus(){
        try {
            this.gitLabManager.validateGitLabInstance();
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
        } catch(GitLabInvalidConfigurationException | IllegalStateException e){
            userService.findUsersWithRoleSystemAdminAndOperator().forEach(user ->
                    notificationService.sendFailureEmail(buildFailureEmail(user)));
            this.monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType(){
        return ServiceType.GITLAB;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }

    private FailureEmail buildFailureEmail(User user){
        return FailureEmail.builder()
                .toEmail(user.getEmail())
                .firstName(Optional.ofNullable(user.getFirstname()).orElse(user.getUsername()))
                .subject("GitLab health check fails")
                .errorMessage("This is to notify you that the GitLab health check fails.")
                .templateName("monitoring-failure-notification")
                .build();
    }
}