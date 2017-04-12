package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.events.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
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
public class AppDeploymentStateChangeManager {

    private final static Logger log = LogManager.getLogger(AppDeploymentStateChangeManager.class);

    @Autowired
    private AppLifecycleRepository repository;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) {
        if (notRelevantDcnDeploymentStateChange(event.getState()))
            return;
        try {
            final Identifier deploymentId = event.getDeploymentId();
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(event.getState());
            Optional<ApplicationEvent> action = repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            log.warn("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            log.warn("State notification failure -> " + e.getMessage());
        }
    }

    boolean notRelevantDcnDeploymentStateChange(DcnDeploymentState state) {
        switch (state) {
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return true;
            default:
                return false;
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyStateChange(NmServiceDeploymentStateChangeEvent event) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(event.getDeploymentId()).nextState(event.getState());
            repository.updateDeploymentState(event.getDeploymentId(), newDeploymentState);
        } catch (InvalidAppStateException e) {
            log.warn("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            log.warn("State notification failure -> " + e.getMessage());
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void notifyGenericError(AppDeploymentErrorEvent event) {
        repository.updateDeploymentState(event.getDeploymentId(), AppDeploymentState.GENERIC_ERROR);
    }

}
