package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    @Autowired
    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    private DcnDeploymentProvider dcnDeployment;

    @Override
    public Identifier deployApplication(Identifier clientId, Identifier applicationId) {

        return null;
    }

    @Override
    public void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) {

    }

    @Override
    public void removeApplication(Identifier deploymentId) {

    }

    @Override
    public void updateApplication(Identifier deploymentId, Identifier applicationId) {

    }

    @Override
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {

    }

}
