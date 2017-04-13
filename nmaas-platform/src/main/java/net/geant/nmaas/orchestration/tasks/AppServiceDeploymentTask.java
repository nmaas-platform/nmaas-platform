package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppDeployServiceActionEvent;
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
public class AppServiceDeploymentTask {

    private final static Logger log = LogManager.getLogger(AppServiceDeploymentTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppServiceDeploymentTask(NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void deployNmService(AppDeployServiceActionEvent event) {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            serviceDeployment.deployNmService(deploymentId);
        } catch (CouldNotDeployNmServiceException e) {
            log.warn("Service deployment failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

}
