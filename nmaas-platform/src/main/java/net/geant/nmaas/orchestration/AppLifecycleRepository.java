package net.geant.nmaas.orchestration;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class AppLifecycleRepository {

    private Map<Identifier, AppDeploymentState> deployments = new HashMap<>();

    public void updateDeploymentState(Identifier deploymentId, AppDeploymentState currentState) {
        deployments.put(deploymentId, currentState);
    }

    public AppDeploymentState loadCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        AppDeploymentState deploymentState = deployments.get(deploymentId);
        if (deploymentState != null)
            return deploymentState;
        else
            throw new InvalidDeploymentIdException(
                    "Deployment with id " + deploymentId + " not found in the repository. ");
    }

}
