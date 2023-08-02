package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AppServiceDeploymentTask {

    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppDeployServiceActionEvent event) {
        try {
            serviceDeployment.deployService(event.getRelatedTo());
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
