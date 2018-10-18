package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;

public interface KServiceLifecycleManager {

    void deployService(Identifier deploymentId);

    boolean checkServiceDeployed(Identifier deploymentId);

    void deleteService(Identifier deploymentId);

    void upgradeService(Identifier deploymentId);

}
