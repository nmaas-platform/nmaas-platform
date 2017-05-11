package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppRemovalTask {

    private final static Logger log = LogManager.getLogger(AppRemovalTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppRemovalTask(NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void remove(AppRemoveActionEvent event) {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            serviceDeployment.removeNmService(deploymentId);
        } catch (CouldNotRemoveNmServiceException e) {
            log.warn("Service removal failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

}
