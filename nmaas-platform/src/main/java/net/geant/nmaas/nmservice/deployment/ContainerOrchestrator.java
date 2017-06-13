package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.Identifier;

import java.util.List;

/**
 * Defines a set of methods each container orchestrator has to implement in order to support NM service deployment.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ContainerOrchestrator {

    /**
     * Provides basic text information about currently used container orchestrator.
     *
     * @return information about the container orchestrator
     */
    String info();

    /**
     * Verifies if currently used container orchestrator is on the list of supported deployment environments specified
     * for NM service being requested.
     *
     * @param supportedDeploymentEnvironments list of deployment environments supported by an application
     * @throws NmServiceRequestVerificationException if none of the application's environments is supported
     */
    void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException;

    /**
     * Checks if requested NM service deployment is possible taking into account available resources, currently
     * running services and other constraints.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws NmServiceRequestVerificationException if service deployment is currently not possible
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException;

    /**
     * Executes all initial configuration steps in order to enable further deployment of the NM service.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotPrepareEnvironmentException if any of the environment preparation steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException;

    /**
     * Performs the actual NM service containers deployment.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployNmServiceException if any of the service deployment steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException;

    /**
     * Checks if NM service was successfully deployed and is running.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws ContainerCheckFailedException if service containers were not deployed successfully
     * @throws DockerNetworkCheckFailedException if service network was not configured successfully
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException;

    /**
     * Triggers all the required actions to remove given NM service from the system.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveNmServiceException if any of the service removal steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException;

}
