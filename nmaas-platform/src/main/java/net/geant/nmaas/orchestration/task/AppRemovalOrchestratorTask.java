package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope("prototype")
public class AppRemovalOrchestratorTask implements Runnable {

    @Autowired
    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    private Identifier deploymentId;

    public void populateIdentifiers(Identifier deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public void run() {
        remove();
    }

    private void remove() {
        verifyIfAllPropertiesAreSet();
        try {
            serviceDeployment.removeNmService(deploymentId);
            dcnDeployment.removeDcn(deploymentId);
        } catch (net.geant.nmaas.nmservice.InvalidDeploymentIdException e) {
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        }
    }

    private void verifyIfAllPropertiesAreSet() {
        if (deploymentId == null)
            throw new NullPointerException();
    }

}
