package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
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
public class DcnRemovalTask {

    private final static Logger log = LogManager.getLogger(DcnRemovalTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public DcnRemovalTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void remove(DcnRemoveActionEvent event) {
        final Identifier clientId = event.getClientId();
        try {
            dcnDeployment.removeDcn(clientId);
        } catch (CouldNotRemoveDcnException e) {
            log.warn("DCN removal failed for client " + clientId.value() + " -> " + e.getMessage());
        }
    }

}
