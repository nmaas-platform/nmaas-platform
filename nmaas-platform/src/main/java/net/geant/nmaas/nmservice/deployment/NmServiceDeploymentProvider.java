package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider {

    NmServiceInfo verifyRequest(Identifier deploymentId, NmServiceSpec serviceSpec) throws NmServiceRequestVerificationException;

    void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException;

    void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException;

    void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException;

    void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException;

}
