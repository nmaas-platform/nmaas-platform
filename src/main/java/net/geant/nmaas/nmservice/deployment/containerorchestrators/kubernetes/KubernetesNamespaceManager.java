package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import net.geant.nmaas.portal.events.DomainCreatedEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Profile("env_kubernetes")
@Log4j2
@RequiredArgsConstructor
public class KubernetesNamespaceManager {

    private final JanitorService janitorService;

    @Value("${nmaas.domains.create.namespace:false}")
    private Boolean triggerNamespaceCreation;

    @Value("${nmaas.domains.namespace.custom.annotations:false}")
    private Boolean includeNamespaceAnnotations;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DomainCreatedEvent event) {
        log.info("Handling DomainCreatedEvent ...");
        if (triggerNamespaceCreation) {
            log.info("Triggering namespace creation using Janitor.");
            List<KeyValueView> annotations = includeNamespaceAnnotations ? event.getDomain().getAnnotations() : Collections.emptyList();
            janitorService.createNameSpace(event.getDomain().getDomainCodename(), annotations);
        } else {
            log.info("Automatic namespace creation is disabled. Nothing to do.");
        }
    }

}
