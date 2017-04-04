package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppRemovalOrchestratorTask {

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    @Autowired
    public AppRemovalOrchestratorTask(
            NmServiceDeploymentProvider serviceDeployment,
            DcnDeploymentProvider dcnDeployment,
            AppDeploymentStateChangeListener appDeploymentStateChangeListener) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.appDeploymentStateChangeListener = appDeploymentStateChangeListener;
    }

    @Loggable(LogLevel.INFO)
    public void remove(Identifier deploymentId) {
        verifyIfAllPropertiesAreSet(deploymentId);
        try {
            serviceDeployment.removeNmService(deploymentId);
            dcnDeployment.removeDcn(deploymentId);
        } catch (net.geant.nmaas.nmservice.InvalidDeploymentIdException e) {
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        }
    }

    private void verifyIfAllPropertiesAreSet(Identifier deploymentId) {
        if (deploymentId == null)
            throw new NullPointerException();
    }

}
