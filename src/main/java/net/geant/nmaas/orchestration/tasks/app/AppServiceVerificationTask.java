package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppServiceVerificationTask {

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppServiceVerificationTask(NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppVerifyServiceActionEvent event) {
        try {
            serviceDeployment.verifyService(event.getRelatedTo());
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
