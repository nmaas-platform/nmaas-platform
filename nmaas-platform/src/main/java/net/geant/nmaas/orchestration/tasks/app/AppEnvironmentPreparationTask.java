package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
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
public class AppEnvironmentPreparationTask {

    private final static Logger log = LogManager.getLogger(AppEnvironmentPreparationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppEnvironmentPreparationTask(
            NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    public void prepareEnvironment(AppPrepareEnvironmentActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getRelatedTo();
        try {
            serviceDeployment.prepareDeploymentEnvironment(deploymentId);
        } catch (CouldNotPrepareEnvironmentException e) {
            log.warn("Service environment preparation failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }
}
