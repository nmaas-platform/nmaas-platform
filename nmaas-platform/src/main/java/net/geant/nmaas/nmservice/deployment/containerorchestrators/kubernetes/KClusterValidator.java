package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;

public interface KClusterValidator {

    void checkClusterStatusAndPrerequisites() throws KClusterCheckException;

}
