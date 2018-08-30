package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Defines a set of methods to manage NM service deployment lifecycle.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider {

    /**
     * Creates new object representing the NM service deployment and verifies if the request can be executed.
     *
     * @param deploymentId unique identifier of service deployment
     * @param deploymentName name of application instance provided by the user
     * @param domain name of the client domain for this deployment
     * @param appDeploymentSpec additional information specific to given application deployment
     * @throws NmServiceRequestVerificationException if service can't be deployed or some input parameters are missing
     */
    void verifyRequest(Identifier deploymentId, String deploymentName, String domain, AppDeploymentSpec appDeploymentSpec) throws NmServiceRequestVerificationException;

    /**
     * Coordinates deployment environment preparation (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @param gitLabRequired indicates if GitLab instance is required during deployment
     * @throws CouldNotPrepareEnvironmentException if environment could't be prepared for some reason
     */
    void prepareDeploymentEnvironment(Identifier deploymentId, boolean gitLabRequired) throws CouldNotPrepareEnvironmentException;

    /**
     * Coordinates NM service deployment (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployNmServiceException if NM service couldn't be deployed for some reason
     */
    void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException;

    /**
     * Coordinates NM service deployment verification (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotVerifyNmServiceException if NM service deployment verification failed
     */
    void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException;

    /**
     * Retrieves deployed service access details to be presented to the client.
     *
     * @param deploymentId unique identifier of service deployment
     * @return service access details
     * @throws CouldNotRetrieveNmServiceAccessDetailsException if access details are not available for any reason
     */
    AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws CouldNotRetrieveNmServiceAccessDetailsException;

    /**
     * Coordinates NM service removal (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveNmServiceException if NM service couldn't be removed for some reason
     */
    void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException;

    /**
     * Coordinates NM service restart (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRestartNmServiceException if NM service couldn't be restarted for some reason
     */
    void restartNmService(Identifier deploymentId) throws CouldNotRestartNmServiceException;

}