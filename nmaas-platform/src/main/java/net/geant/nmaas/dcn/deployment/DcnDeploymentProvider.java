package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Defines a set of methods to manage DCN deployment lifecycle.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface DcnDeploymentProvider {

    /**
     * Checks current state of given client's DCN.
     *
     * @param clientId identifier of the client
     * @return DCN state information
     */
    DcnState checkState(Identifier clientId);

    /**
     * Verifies if requested DCN for given client can be deployed.
     *
     * @param clientId identifier of the client
     * @param dcnSpec specification of the DCN
     * @throws DcnRequestVerificationException if request verification failed
     */
    void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException;

    /**
     * Performs actual deployment of the DCN for given client.
     *
     * @param clientId identifier of the client
     * @throws CouldNotDeployDcnException if DCN deployment couldn't be completed due to any reason
     */
    void deployDcn(Identifier clientId) throws CouldNotDeployDcnException;

    /**
     * Verifies a successful deployment of a DCN for given client.
     *
     * @param clientId identifier of the client
     * @throws CouldNotVerifyDcnException if DCN deployment couldn't be verified
     */
    void verifyDcn(Identifier clientId) throws CouldNotVerifyDcnException;

    /**
     * Removes DCN for given client. Removal is only possible when there are no NM services currently deployed
     * by the client.
     *
     * @param clientId identifier of the client
     * @throws CouldNotRemoveDcnException if DCN removal process failed
     */
    void removeDcn(Identifier clientId) throws CouldNotRemoveDcnException;

}
