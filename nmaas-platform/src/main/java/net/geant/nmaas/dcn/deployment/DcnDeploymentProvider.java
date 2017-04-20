package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DcnDeploymentProvider {

    void verifyRequest(Identifier deploymentId, DcnSpec dcnSpec) throws DcnRequestVerificationException;

    void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareDcnException;

    void deployDcn(Identifier deploymentId) throws CouldNotDeployDcnException;

    void verifyDcn(Identifier deploymentId) throws CouldNotVerifyDcnException;

    void removeDcn(Identifier deploymentId) throws CouldNotRemoveDcnException;

}
