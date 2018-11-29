package net.geant.nmaas.orchestration;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.orchestration.events.app.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class AppDeploymentStateChangeManager {

    private AppDeploymentRepositoryManager deploymentRepositoryManager;

    private ApplicationEventPublisher eventPublisher;

    private NotificationService notificationService;

    private UserService userService;

    private DomainService domains;

    private AppDeploymentMonitor appDeploymentMonitor;

    @Autowired
    public AppDeploymentStateChangeManager(AppDeploymentRepositoryManager deploymentRepositoryManager,
                                           ApplicationEventPublisher eventPublisher,
                                           NotificationService notificationService,
                                           UserService userService,
                                           DomainService domains,
                                           AppDeploymentMonitor appDeploymentMonitor){
        this.deploymentRepositoryManager = deploymentRepositoryManager;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.userService = userService;
        this.domains = domains;
        this.appDeploymentMonitor = appDeploymentMonitor;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized ApplicationEvent notifyStateChange(NmServiceDeploymentStateChangeEvent event) {
        try {
            AppDeploymentState newDeploymentState = deploymentRepositoryManager.loadState(event.getDeploymentId()).nextState(event.getState());
            deploymentRepositoryManager.updateState(event.getDeploymentId(), newDeploymentState);
            if(newDeploymentState == AppDeploymentState.REQUEST_VALIDATION_FAILED ||
                    newDeploymentState == AppDeploymentState.APPLICATION_REMOVAL_FAILED ||
                    newDeploymentState == AppDeploymentState.APPLICATION_DEPLOYMENT_FAILED ||
                    newDeploymentState == AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED ||
                    newDeploymentState == AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED ||
                    newDeploymentState == AppDeploymentState.APPLICATION_RESTART_FAILED ||
                    newDeploymentState == AppDeploymentState.APPLICATION_CONFIGURATION_FAILED ||
                    newDeploymentState == AppDeploymentState.INTERNAL_ERROR ||
                    newDeploymentState == AppDeploymentState.REQUEST_VALIDATED){
                log.warn("Application deployment failed state detected. Saving error message: " +
                        event.getErrorMessage());
                deploymentRepositoryManager.reportErrorStatusAndSaveInEntity(event.getDeploymentId(), event.getErrorMessage());
            }
            if(newDeploymentState == AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED) {
                Optional<AppDeployment> optionalAppDeployment = deploymentRepositoryManager.load(event.getDeploymentId());
                if (optionalAppDeployment.isPresent()) {
                    AppDeployment appDeployment = optionalAppDeployment.get();
                    final User user = userService.findByUsername(appDeployment.getLoggedInUsersName()).orElseThrow(ProcessingException::new);
                    final List<User> domainUsers = domains.findUsersWithDomainAdminRole(appDeployment.getDomainId());

                    notificationService.sendEmail(getAppInstanceReadyEmailConfirmation(user, appDeployment));
                    domainUsers.forEach(domainUser ->
                            notificationService.sendEmail(getDomainAdminNotificationEmailConfirmation(domainUser, appDeployment)));
                }
            }
            return triggerActionEventIfRequired(event.getDeploymentId(), newDeploymentState).orElse(null);
        } catch (InvalidAppStateException e) {
            log.warn("State notification failure -> " + e.getMessage());
            deploymentRepositoryManager.reportErrorStatusAndSaveInEntity(event.getDeploymentId(), e.getMessage());
            deploymentRepositoryManager.updateState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
            return null;
        }
    }

    private Optional<ApplicationEvent> triggerActionEventIfRequired(Identifier deploymentId, AppDeploymentState currentState) {
        switch (currentState) {
            case REQUEST_VALIDATED:
                return Optional.of(new AppPrepareEnvironmentActionEvent(this, deploymentId));
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
                return Optional.of(new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId));
            case MANAGEMENT_VPN_CONFIGURED:
                return Optional.of(new AppVerifyConfigurationActionEvent(this, deploymentId));
            case APPLICATION_CONFIGURED:
                return Optional.of(new AppDeployServiceActionEvent(this, deploymentId));
            case APPLICATION_DEPLOYED:
                return Optional.of(new AppVerifyServiceActionEvent(this, deploymentId));
            case APPLICATION_REMOVED:
                return Optional.of(new AppRemoveDcnIfRequiredEvent(this, deploymentId));
            default:
                return Optional.empty();
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized void notifyGenericError(AppDeploymentErrorEvent event) {
        try{
            deploymentRepositoryManager.updateState(event.getRelatedTo(), AppDeploymentState.GENERIC_ERROR);
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized void notifyDcnDeployed(DcnDeployedEvent event) {
        try{
            deploymentRepositoryManager.loadAllWaitingForDcn(event.getRelatedTo())
                    .forEach(d -> eventPublisher.publishEvent(
                            new NmServiceDeploymentStateChangeEvent(this, d.getDeploymentId(), NmServiceDeploymentState.READY_FOR_DEPLOYMENT, "")));
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    private ConfirmationEmail getAppInstanceReadyEmailConfirmation(User user, AppDeployment appDeployment){
        return ConfirmationEmail.builder()
                .toEmail(user.getEmail())
                .firstName(Optional.ofNullable(user.getFirstname()).orElse(user.getUsername()))
                .lastName(user.getLastname())
                .appInstanceName(appDeployment.getAppInstanceName())
                .appName(appDeployment.getAppName())
                .domainName(appDeployment.getDomain())
                .accessURL(this.appDeploymentMonitor.userAccessDetails(appDeployment.getDeploymentId()).getUrl())
                .subject("Your app instance is ready")
                .templateName("app-instance-ready-notification")
                .build();
    }

    private ConfirmationEmail getDomainAdminNotificationEmailConfirmation(User user, AppDeployment appDeployment){
        return ConfirmationEmail.builder()
                .toEmail(user.getEmail())
                .firstName(Optional.ofNullable(user.getFirstname()).orElse(user.getUsername()))
                .lastName(user.getLastname())
                .appInstanceName(appDeployment.getAppInstanceName())
                .appName(appDeployment.getAppName())
                .domainName(appDeployment.getDomain())
                .accessURL(this.appDeploymentMonitor.userAccessDetails(appDeployment.getDeploymentId()).getUrl())
                .subject("New app instance is ready")
                .templateName("domain-admin-notification")
                .build();
    }

}
