package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcndeployment.DcnDeploymentCoordinator;
import net.geant.nmaas.nmservicedeployment.ServiceDeploymentCoordinator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class BasicAppDeploymentOrchestrator implements AppDeploymentOrchestrator {

    @Autowired
    private ServiceDeploymentCoordinator serviceDeploymentCoordinator;

    @Autowired
    private DcnDeploymentCoordinator dcnDeploymentCoordinator;

    @Override
    public String deployApplication(String clientId, String applicationId) {
        return null;
    }

    @Override
    public void applyConfiguration(String deploymentId, AppConfiguration configuration) {

    }

    @Override
    public void removeApplication(String deploymentId) {

    }

    @Override
    public void updateApplication(String deploymentId, String applicationId) {

    }

    @Override
    public void updateConfiguration(String deploymentId, AppConfiguration configuration) {

    }
}
