package net.geant.nmaas.orchestration.tasks.app;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.NmServiceDeployment;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppConfigurationUpdateTask {

    private final NmServiceConfigurationProvider configurationProvider;
    private final DefaultAppDeploymentRepositoryManager repositoryManager;
    private final RemoteClusterManagementService remoteClusterManager;

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppUpdateConfigurationEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final AppDeployment appDeployment = repositoryManager.load(deploymentId);
            final AppDeploymentOwner appDeploymentOwner = repositoryManager.loadOwner(deploymentId);
            final NmServiceDeployment nmServiceDeployment = NmServiceDeployment.fromAppDeployment(appDeployment, appDeploymentOwner);
            if (Objects.nonNull(appDeployment.getRemoteClusterId())) {
                nmServiceDeployment.setRemoteCluster(remoteClusterManager.getClusterEntity(appDeployment.getRemoteClusterId()));
            }
            configurationProvider.updateNmService(nmServiceDeployment);
        } catch (Exception e) {
            log.error("Error reported at {}", LocalDateTime.now(), e);
        }
    }
}
