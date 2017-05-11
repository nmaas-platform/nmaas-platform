package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DcnDeploymentProvider {

    boolean checkIfExists(Identifier clientId);

    void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException;

    void deployDcn(Identifier clientId) throws CouldNotDeployDcnException;

    void verifyDcn(Identifier clientId) throws CouldNotVerifyDcnException;

    void removeDcn(Identifier clientId) throws CouldNotRemoveDcnException;

}
