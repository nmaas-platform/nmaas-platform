package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KClusterValidator;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.stereotype.Component;

@Component
public class DefaultKClusterValidator implements KClusterValidator {

    /**
     * Checks if defined requirements are met by the Kubernetes cluster.
     * List of requirements can be easily extended.
     *
     * @throws KClusterCheckException if requirements are not met
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void checkClusterStatusAndPrerequisites() {
        // TODO this check should be delegated to a dedicated external component which does not yet exist
    }

}
