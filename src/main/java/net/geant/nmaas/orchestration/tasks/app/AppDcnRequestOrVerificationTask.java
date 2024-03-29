package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AppDcnRequestOrVerificationTask {

    private final DefaultAppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final DcnDeploymentProvidersManager providersManager;

    /**
     * Checks current state of DCN for given client and depending on the result requests new DCN deployment
     * (or re-deployment), publishes notification that DCN is already deployed and running or does nothing.
     *
     * @param event object of type {@link AppRequestNewOrVerifyExistingDcnEvent}
     * @return action event to be processed by the system or <code>null</code> if no action is required
     * @throws InvalidDeploymentIdException if no deployment with given identifier exists in the system
     */
    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent trigger(AppRequestNewOrVerifyExistingDcnEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final String domain = appDeploymentRepositoryManager.loadDomain(deploymentId);
            switch(providersManager.getDcnDeploymentProvider(domain).checkState(domain)) {
                case NONE:
                case REMOVED:
                    return dcnDeploymentEvent(domain);
                case DEPLOYED:
                    return dcnReadyNotificationEvent(deploymentId);
                case PROCESSED:
                    return noEvent();
            }
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
        return noEvent();
    }

    private DcnVerifyRequestActionEvent dcnDeploymentEvent(String domain) {
        return new DcnVerifyRequestActionEvent(this, domain);
    }

    private NmServiceDeploymentStateChangeEvent dcnReadyNotificationEvent(Identifier deploymentId) {
        return new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.READY_FOR_DEPLOYMENT, "");
    }

    private ApplicationEvent noEvent() {
        return null;
    }

}
