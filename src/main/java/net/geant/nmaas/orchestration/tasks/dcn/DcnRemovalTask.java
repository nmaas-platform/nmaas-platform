package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DcnRemovalTask extends BaseDcnTask {

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DcnRemoveActionEvent event) {
        try {
            final String domain = event.getRelatedTo();
            providersManager.getDcnDeploymentProvider(domain).removeDcn(domain);
        } catch (Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
