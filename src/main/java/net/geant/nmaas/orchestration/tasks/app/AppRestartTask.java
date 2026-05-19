package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppRestartTask {

    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppRestartActionEvent event) {
        try {
            if (event.getUsername() != null) {
                serviceDeployment.restartService(event.getRelatedTo(), event.getUsername());
            } else {
                serviceDeployment.restartService(event.getRelatedTo());
            }
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
        }
    }

}
