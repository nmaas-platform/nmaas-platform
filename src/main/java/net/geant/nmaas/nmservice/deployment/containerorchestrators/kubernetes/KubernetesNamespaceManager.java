package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiJanitorService;
import net.geant.nmaas.portal.events.DomainCreatedEvent;
import net.geant.nmaas.portal.events.RemoteClusterNamespaceEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesNamespaceManager {

    private final KubernetesApiJanitorService janitorService;

    @Value("${nmaas.domains.create.namespace:false}")
    private Boolean triggerNamespaceCreation;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DomainCreatedEvent event) {
        log.info("Handling DomainCreatedEvent ...");
        if (triggerNamespaceCreation) {
            log.info("Triggering namespace creation using API client service.");
            janitorService.createNamespace(event.getDomain().domainCodename(), event.getDomain().annotations());
        } else {
            log.info("Automatic namespace creation is disabled. Nothing to do.");
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(RemoteClusterNamespaceEvent event) {
        log.info("Handling RemoteClusterNamespaceEvent ...");
        log.info("... not yet implemented");
    }

}