package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DcnDeploymentProvider {

    void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec);

    void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException;

    void deployDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

    void verifyDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

    void removeDcn(Identifier deploymentId) throws InvalidDeploymentIdException;

}
