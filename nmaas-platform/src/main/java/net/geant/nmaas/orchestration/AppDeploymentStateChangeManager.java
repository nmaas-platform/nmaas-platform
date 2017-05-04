package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.*;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class AppDeploymentStateChangeManager {

    private final static Logger log = LogManager.getLogger(AppDeploymentStateChangeManager.class);

    @Autowired
    private AppDeploymentRepositoryManager lifecycleStateKeeper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized ApplicationEvent notifyStateChange(NmServiceDeploymentStateChangeEvent event) throws InvalidDeploymentIdException {
        try {
            AppDeploymentState newDeploymentState = lifecycleStateKeeper.loadState(event.getDeploymentId()).nextState(event.getState());
            lifecycleStateKeeper.updateState(event.getDeploymentId(), newDeploymentState);
            return triggerActionEventIfRequired(event.getDeploymentId(), newDeploymentState).orElse(null);
        } catch (InvalidAppStateException e) {
            log.warn("State notification failure -> " + e.getMessage());
            lifecycleStateKeeper.updateState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
            return null;
        }
    }

    private Optional<ApplicationEvent> triggerActionEventIfRequired(Identifier deploymentId, AppDeploymentState currentState) {
        switch (currentState) {
            case REQUEST_VALIDATED:
                return Optional.of(new AppPrepareEnvironmentActionEvent(this, deploymentId));
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
                return Optional.of(new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId));
            case APPLICATION_CONFIGURED:
                return Optional.of(new AppDeployServiceActionEvent(this, deploymentId));
            case APPLICATION_DEPLOYED:
                return Optional.of(new AppVerifyServiceActionEvent(this, deploymentId));
            default:
                return Optional.empty();
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized void notifyGenericError(AppDeploymentErrorEvent event) throws InvalidDeploymentIdException {
        lifecycleStateKeeper.updateState(event.getDeploymentId(), AppDeploymentState.GENERIC_ERROR);
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized void notifyDcnDeployed(DcnDeployedEvent event) {
        lifecycleStateKeeper.getAllWaitingForDcn(event.getClientId())
                .forEach(d -> eventPublisher.publishEvent(
                        new NmServiceDeploymentStateChangeEvent(this, d.getDeploymentId(), NmServiceDeploymentState.READY_FOR_DEPLOYMENT)));
    }

}
