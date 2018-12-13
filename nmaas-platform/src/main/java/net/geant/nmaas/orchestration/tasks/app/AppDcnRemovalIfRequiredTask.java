package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network.DockerHostNetworkRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
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
    public ApplicationEvent trigger(AppRemoveDcnIfRequiredEvent event) {
        try {
            final Identifier deploymentId = event.getRelatedTo();
            final String domain = appDeploymentRepositoryManager.loadDomainByDeploymentId(deploymentId);
            //TODO
            //return dockerHostNetworkRepositoryManager.checkNetwork(domain) ? null : new DcnRemoveActionEvent(this, domain);
            return null;
        } catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
            return null;
        }
    }

}
