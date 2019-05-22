package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;

/**
 * Defines a set of methods to manage DCN deployment lifecycle.
 */
public interface DcnDeploymentProvider {

    /**
     * Checks current state of DCN deployed for given domain.
     *
     * @param domain name of the domain
     * @return DCN state information
     */
    DcnState checkState(String domain);

    /**
     * Verifies if the requested DCN for given domain can be deployed.
     *
     * @param domain name of the domain
     * @param dcnSpec specification of the DCN
     * @throws DcnRequestVerificationException if request verification failed
     */
    void verifyRequest(String domain, DcnSpec dcnSpec);

    /**
     * Performs actual deployment of the DCN for given domain.
     *
     * @param domain name of the domain
     * @throws CouldNotDeployDcnException if DCN deployment couldn't be completed due to any reason
     */
    void deployDcn(String domain);

    /**
     * Verifies the successful deployment of the DCN for given domain.
     *
     * @param domain name of the domain
     * @throws CouldNotVerifyDcnException if DCN deployment couldn't be verified
     */
    void verifyDcn(String domain);

    /**
     * Removes the DCN for given domain. Removal is only possible if there are no NM services currently deployed
     * for this domain.
     *
     * @param domain name of the domain
     * @throws CouldNotRemoveDcnException if DCN removal process failed
     */
    void removeDcn(String domain);

    DcnDeploymentType getDcnDeploymentType();

}
