package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.deploymentorchestration.AppDeploymentStateChanger;
import net.geant.nmaas.deploymentorchestration.Identifier;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider extends AppDeploymentStateChanger {

    void verifyRequest(Identifier deploymentId, NmServiceSpec serviceSpec);

    void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException;

    void deployNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

    void verifyNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

    void removeNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

}
