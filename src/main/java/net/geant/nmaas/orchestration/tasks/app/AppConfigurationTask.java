package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.NmServiceDeployment;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateBasicAuthActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppConfigurationTask {

    private final NmServiceConfigurationProvider configurationProvider;
    private final DefaultAppDeploymentRepositoryManager repositoryManager;
    private final RemoteClusterManagementService remoteClusterManager;

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppApplyConfigurationActionEvent event) throws InterruptedException {
        Thread.sleep(1000);
        try {
            final NmServiceDeployment nmServiceDeployment = prepareServiceDeployment(event.getRelatedTo());
            if (Objects.isNull(nmServiceDeployment.getAppConfiguration())) {
                log.warn("Application configuration of deployment {} is null", nmServiceDeployment.getDescriptiveDeploymentId());
            }
            if (event.getUserInitiator() != null) {
                configurationProvider.configureNmService(nmServiceDeployment, event.getUserInitiator());

            } else {
                configurationProvider.configureNmService(nmServiceDeployment);
            }
            configurationProvider.configureNmService(nmServiceDeployment);
        } catch (Exception ex) {
            log.error("Exception during task execution", ex);
        }
    }

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public void trigger(AppUpdateBasicAuthActionEvent event) {
        try {
            final NmServiceDeployment nmServiceDeployment = prepareServiceDeployment(event.getRelatedTo());
            configurationProvider.configureBasicAuth(nmServiceDeployment, event.getBasicAuthUsername(), event.getBasicAuthPassword());
        } catch (Exception ex) {
            log.error("Exception during task execution", ex);
        }
    }

    private NmServiceDeployment prepareServiceDeployment(Identifier deploymentId) {
        final AppDeployment appDeployment = repositoryManager.load(deploymentId);
        final AppDeploymentOwner appDeploymentOwner = repositoryManager.loadOwner(deploymentId);
        final NmServiceDeployment nmServiceDeployment = NmServiceDeployment.fromAppDeployment(appDeployment, appDeploymentOwner);
        if (Objects.nonNull(appDeployment.getRemoteClusterId())) {
            nmServiceDeployment.setRemoteCluster(remoteClusterManager.getClusterEntity(appDeployment.getRemoteClusterId()));
        }
        return nmServiceDeployment;
    }

}
