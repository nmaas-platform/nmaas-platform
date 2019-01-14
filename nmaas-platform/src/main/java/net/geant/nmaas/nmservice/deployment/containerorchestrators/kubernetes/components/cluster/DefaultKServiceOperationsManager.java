package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("env_kubernetes")
public class DefaultKServiceOperationsManager implements KServiceOperationsManager {

    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) {
        throw new NotImplementedException();
    }
}
