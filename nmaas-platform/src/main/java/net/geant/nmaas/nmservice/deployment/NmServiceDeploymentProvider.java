package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider extends AppDeploymentStateChanger {

    NmServiceInfo verifyRequest(Identifier deploymentId, NmServiceSpec serviceSpec);

    void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException;

    void deployNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

    void verifyNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

    void removeNmService(Identifier deploymentId) throws InvalidDeploymentIdException;

}
