package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppRemoveFailedActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppRemoveFailedActionTask {

    private final NmServiceDeploymentProvider serviceDeployment;
    private final NmServiceConfigurationProvider configurationProvider;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppRemoveFailedActionEvent event) throws InterruptedException {
        try {
            this.serviceDeployment.removeService(event.getRelatedTo());
            this.configurationProvider.removeNmService(event.getRelatedTo());
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
        }
        Thread.sleep(1000);
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, event.getRelatedTo(), ServiceDeploymentState.FAILED_APPLICATION_REMOVED, ""));
    }

}