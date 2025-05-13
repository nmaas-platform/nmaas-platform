package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class DcnDeploymentTask extends BaseDcnTask {

    @Autowired
    public DcnDeploymentTask(DcnDeploymentProvidersManager providersManager) {
        super(providersManager);
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DcnDeployActionEvent event) {
        try {
            final String domain = event.getRelatedTo();
            providersManager.getDcnDeploymentProvider(domain).deployDcn(domain);
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
        }
    }

}
