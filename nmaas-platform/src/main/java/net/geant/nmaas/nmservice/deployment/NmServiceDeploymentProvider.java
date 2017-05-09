package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider {

    void verifyRequest(Identifier deploymentId, Identifier clientId, DockerContainerTemplate template) throws NmServiceRequestVerificationException;

    void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException;

    void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException;

    void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException;

    void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException;

}
