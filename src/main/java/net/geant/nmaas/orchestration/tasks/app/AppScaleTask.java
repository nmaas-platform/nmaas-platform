package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppScaleActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppScaleTask {

    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void handleScaleEvent(AppScaleActionEvent event) {
        try {
            switch (event.getDirection()) {
                case DOWN:
                    serviceDeployment.pauseService(event.getDeploymentId(), event.getUserInitiator());
                    break;
                case UP:
                    serviceDeployment.resumeService(event.getDeploymentId(), event.getUserInitiator());
                    break;
            }
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
        }
    }
}
