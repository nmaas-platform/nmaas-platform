package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppDcnRequestOrVerificationTask {

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public AppDcnRequestOrVerificationTask(
            AppDeploymentRepositoryManager appDeploymentRepositoryManager,
            DcnDeploymentProvider dcnDeployment) {
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.dcnDeployment = dcnDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent requestOrVerifyDcn(AppRequestNewOrVerifyExistingDcnEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        final Identifier clientId = appDeploymentRepositoryManager.loadClientIdByDeploymentId(deploymentId);
        return dcnDeployment.checkIfExists(clientId)
                ? new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.READY_FOR_DEPLOYMENT)
                : new DcnVerifyRequestActionEvent(this, clientId);
    }

}
