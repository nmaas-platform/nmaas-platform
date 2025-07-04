package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppScaleActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppScaleTask {

    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void handleScaleEvent(AppScaleActionEvent event) {
        switch (event.getDirection()) {
            case DOWN:
                serviceDeployment.pauseService(event.getDeploymentId());
                break;
            case UP:
                serviceDeployment.resumeService(event.getDeploymentId());
                break;
        }
    }
}
