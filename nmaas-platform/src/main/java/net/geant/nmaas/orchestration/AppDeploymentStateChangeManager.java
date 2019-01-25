package net.geant.nmaas.orchestration;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
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
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class AppDeploymentStateChangeManager {

    private AppDeploymentRepositoryManager deploymentRepositoryManager;

    private AppDeploymentMonitor deploymentMonitor;

    private ApplicationEventPublisher eventPublisher;


    @Autowired
    public AppDeploymentStateChangeManager(AppDeploymentRepositoryManager deploymentRepositoryManager,
                                           AppDeploymentMonitor deploymentMonitor,
                                           ApplicationEventPublisher eventPublisher){
        this.deploymentRepositoryManager = deploymentRepositoryManager;
        this.deploymentMonitor = deploymentMonitor;
        this.eventPublisher = eventPublisher;
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
                deploymentRepositoryManager.load(event.getDeploymentId())
                        .ifPresent(appDeployment -> eventPublisher.publishEvent(new NotificationEvent(this, getMailAttributes(appDeployment))));
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

    private MailAttributes getMailAttributes(AppDeployment appDeployment){
        return MailAttributes.builder()
                .otherAttributes(ImmutableMap.of(
                        "accessURL" ,deploymentMonitor.userAccessDetails(appDeployment.getDeploymentId()).getUrl(),
                        "domainName", appDeployment.getDomain(),
                        "owner", appDeployment.getOwner(),
                        "appInstanceName",appDeployment.getDeploymentName(),
                        "appName",appDeployment.getAppName()
                ))
                .mailType(MailType.APP_DEPLOYED)
                .build();
    }

}
