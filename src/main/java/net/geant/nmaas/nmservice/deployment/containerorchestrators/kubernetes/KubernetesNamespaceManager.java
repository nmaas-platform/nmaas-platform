package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiJanitorService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
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
    private final KClusterRepository kClusterRepository;

    @Value("${nmaas.domains.create.namespace:false}")
    private Boolean triggerNamespaceCreation;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DomainCreatedEvent event) {
        log.info("Handling DomainCreatedEvent ...");
        if (triggerNamespaceCreation) {
            log.info("Triggering namespace creation using API client service.");
            // TODO currently domain codename is used as namespace name, this might not be ok in some cases
            janitorService.createNamespace(event.getDomain().domainCodename(), event.getDomain().annotations());
        } else {
            log.info("Automatic namespace creation is disabled. Nothing to do.");
        }
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(RemoteClusterNamespaceEvent event) {
        log.info("Handling RemoteClusterNamespaceEvent ...");
        final KCluster kCluster = kClusterRepository.getReferenceById(event.getRemoteClusterId());
        log.info("Checking if namespace {} already exists on cluster {}", event.getDomainCodename(), kCluster.getCodename());
        if (!janitorService.checkIfNamespaceExists(kCluster, event.getDomainCodename())) {
            log.info("Triggering namespace creation on a remote cluster using API client service.");
            // TODO currently domain codename is used as namespace name, this might not be ok in some cases
            janitorService.createNamespace(kCluster, event.getDomainCodename(), event.getAnnotations());
        }
    }

}