package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;

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
     * for NM service being requested and if so creates proper NM service info object.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param domain name of the client domain for this deployment
     * @param appDeploymentSpec additional information specific to given application deployment
     * @throws NmServiceRequestVerificationException if current deployment environment is not supported by the application
     */
    void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, Identifier applicationId, String domain, AppDeploymentSpec appDeploymentSpec)
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
     * Retrieves deployed service access details to be presented to the client.
     *
     * @param deploymentId unique identifier of service deployment
     * @return service access details
     * @throws ContainerOrchestratorInternalErrorException if access details are not available for any reason
     */
    AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException;

    /**
     * Triggers all the required actions to remove given NM service from the system.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveNmServiceException if any of the service removal steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException;

    /**
     * Triggers all the required actions to restart given NM service.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRestartNmServiceException if any of the service restart steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void restartNmService(Identifier deploymentId)
            throws CouldNotRestartNmServiceException, ContainerOrchestratorInternalErrorException;
}
