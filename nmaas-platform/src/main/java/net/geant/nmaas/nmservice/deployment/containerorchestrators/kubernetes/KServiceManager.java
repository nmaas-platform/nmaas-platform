package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

public interface KServiceManager extends KNamespaceAware {

    void deployService(Identifier deploymentId) throws KServiceManipulationException, InvalidDeploymentIdException;

    boolean checkServiceDeployed(Identifier deploymentId) throws KServiceManipulationException;

    void deleteService(Identifier deploymentId) throws KServiceManipulationException;

}
