package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DcnDeploymentStateChangeManager {

    private final static Logger log = LogManager.getLogger(DcnDeploymentStateChangeManager.class);

    @Autowired
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @EventListener
    @Loggable(LogLevel.INFO)
    public synchronized ApplicationEvent triggerActionOnStateChange(DcnDeploymentStateChangeEvent event) {
        return triggerActionEventIfRequired(event.getClientId(), event.getState()).orElse(null);
    }

    private Optional<ApplicationEvent> triggerActionEventIfRequired(Identifier clientId, DcnDeploymentState currentState) {
        switch (currentState) {
            case REQUEST_VERIFIED:
                return Optional.of(new DcnDeployActionEvent(this, clientId));
            case DEPLOYED:
                return Optional.of(new DcnVerifyActionEvent(this, clientId));
            case VERIFIED:
                return Optional.of(new DcnDeployedEvent(this, clientId));
            default:
                return Optional.empty();
        }
    }

}
