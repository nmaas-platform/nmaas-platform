package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DcnDeploymentProvider extends AppDeploymentStateChanger {

    void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec);

    void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException;

    void deployDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

    void verifyDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

    void removeDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

}
