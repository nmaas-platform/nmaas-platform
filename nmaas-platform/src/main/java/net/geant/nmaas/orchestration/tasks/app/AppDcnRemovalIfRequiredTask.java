package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerHostNetworkRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppDcnRemovalIfRequiredTask {

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private DockerHostNetworkRepositoryManager dockerHostNetworkRepositoryManager;

    @Autowired
    public AppDcnRemovalIfRequiredTask(
            AppDeploymentRepositoryManager appDeploymentRepositoryManager,
            DockerHostNetworkRepositoryManager dockerHostNetworkRepositoryManager) {
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.dockerHostNetworkRepositoryManager = dockerHostNetworkRepositoryManager;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent removeDcnIfRequired(AppRemoveDcnIfRequiredEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getRelatedTo();
        final Identifier clientId = appDeploymentRepositoryManager.loadClientIdByDeploymentId(deploymentId);
        return dockerHostNetworkRepositoryManager.checkNetwork(clientId) ? null : new DcnRemoveActionEvent(this, clientId);
    }

}
