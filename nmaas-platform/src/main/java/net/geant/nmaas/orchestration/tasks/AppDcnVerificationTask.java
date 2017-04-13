package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppVerifyDcnActionEvent;
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
public class AppDcnVerificationTask {

    private final static Logger log = LogManager.getLogger(AppDcnVerificationTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public AppDcnVerificationTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @EventListener
    public void deployDcn(AppVerifyDcnActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            dcnDeployment.verifyDcn(deploymentId);
        } catch (CouldNotVerifyDcnException e) {
            log.warn("DCN verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }
}
