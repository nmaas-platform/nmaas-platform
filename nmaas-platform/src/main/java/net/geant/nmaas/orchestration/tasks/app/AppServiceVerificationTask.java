package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class AppServiceVerificationTask {

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppServiceVerificationTask(NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppVerifyServiceActionEvent event) throws InvalidDeploymentIdException, CouldNotVerifyNmServiceException {
        serviceDeployment.verifyNmService(event.getRelatedTo());
    }

}
