package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.portal.events.ApplicationActivatedEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelmRepoUpdateListener {

    private final HelmKServiceManager helmKServiceManager;

    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent trigger(ApplicationActivatedEvent event) {
        helmKServiceManager.updateHelmRepo();
        return null;
    }

}
