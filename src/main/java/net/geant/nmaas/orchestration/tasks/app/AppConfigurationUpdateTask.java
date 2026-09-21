package net.geant.nmaas.orchestration.tasks.app;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppConfigurationUpdateTask {

    private final NmServiceDeploymentProvider serviceDeployment;

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppUpdateConfigurationActionEvent event) throws InterruptedException {
        Thread.sleep(1000);
        try {
            final Identifier deploymentId = event.getRelatedTo();
            serviceDeployment.updateKubernetesService(deploymentId, event.getUserInitiator());
        } catch (Exception e) {
            log.error("Error reported at {}", LocalDateTime.now(), e);
        }
    }
}
