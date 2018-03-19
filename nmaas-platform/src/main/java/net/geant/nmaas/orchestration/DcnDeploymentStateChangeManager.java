package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DcnDeploymentStateChangeManager {

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized ApplicationEvent triggerActionOnStateChange(DcnDeploymentStateChangeEvent event) {
        return triggerActionEventIfRequired(event.getDomain(), event.getState()).orElse(null);
    }

    private Optional<ApplicationEvent> triggerActionEventIfRequired(String domain, DcnDeploymentState currentState) {
        switch (currentState) {
            case REQUEST_VERIFIED:
                return Optional.of(new DcnDeployActionEvent(this, domain));
            case DEPLOYED:
                return Optional.of(new DcnVerifyActionEvent(this, domain));
            case VERIFIED:
                return Optional.of(new DcnDeployedEvent(this, domain));
            default:
                return Optional.empty();
        }
    }

}
