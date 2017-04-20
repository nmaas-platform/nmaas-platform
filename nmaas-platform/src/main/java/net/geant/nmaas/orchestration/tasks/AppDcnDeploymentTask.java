package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppDeployDcnActionEvent;
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
public class AppDcnDeploymentTask {

    private final static Logger log = LogManager.getLogger(AppDcnDeploymentTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public AppDcnDeploymentTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @EventListener
    public void deployDcn(AppDeployDcnActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            dcnDeployment.deployDcn(deploymentId);
        } catch (CouldNotDeployDcnException e) {
            log.warn("DCN deployment failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }
}
