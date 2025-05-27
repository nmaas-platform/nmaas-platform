package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;

public interface KServiceLifecycleManager {

    void deployService(Identifier deploymentId);

    boolean checkServiceDeployed(Identifier deploymentId);

    void deleteServiceIfExists(Identifier deploymentId);

    void upgradeService(Identifier deploymentId, KubernetesTemplate targetVersion);

    void updateHelmRepo();

    void scaleDeployment(AppDeployment deployment, int replicas);


}
