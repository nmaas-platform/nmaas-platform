package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppVerifyDeploymentActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppDeploymentVerificationTask {

    private final static Logger log = LogManager.getLogger(AppDeploymentVerificationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public AppDeploymentVerificationTask(NmServiceDeploymentProvider serviceDeployment, DcnDeploymentProvider dcnDeployment) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
    }

    @Async
    @EventListener
    public void verifyDeployment(AppVerifyDeploymentActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            serviceDeployment.verifyNmService(deploymentId);
            dcnDeployment.verifyDcn(deploymentId);
        } catch (CouldNotVerifyNmServiceException e) {
            log.warn("Service verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        } catch (CouldNotVerifyDcnException e) {
            log.warn("DCN verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

}
