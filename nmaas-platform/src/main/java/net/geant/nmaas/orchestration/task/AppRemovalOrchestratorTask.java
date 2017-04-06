package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppRemovalOrchestratorTask {

    private final static Logger log = LogManager.getLogger(AppDeploymentOrchestratorTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AppRemovalOrchestratorTask(
            NmServiceDeploymentProvider serviceDeployment,
            DcnDeploymentProvider dcnDeployment,
            ApplicationEventPublisher applicationEventPublisher) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Loggable(LogLevel.INFO)
    public void remove(Identifier deploymentId) {
        verifyIfAllPropertiesAreSet(deploymentId);
        try {
            serviceDeployment.removeNmService(deploymentId);
            dcnDeployment.removeDcn(deploymentId);
        } catch (net.geant.nmaas.nmservice.InvalidDeploymentIdException e) {
            log.error("Exception during application removal -> " + e.getMessage());
            applicationEventPublisher.publishEvent(new AppDeploymentErrorEvent(this, deploymentId));
        }
    }

    private void verifyIfAllPropertiesAreSet(Identifier deploymentId) {
        if (deploymentId == null)
            throw new NullPointerException();
    }

}
