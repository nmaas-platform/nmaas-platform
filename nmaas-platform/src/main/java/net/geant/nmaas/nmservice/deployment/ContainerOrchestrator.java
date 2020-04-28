package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;

/**
 * Defines a set of methods each container orchestrator has to implement in order to support NM service deployment.
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
     * @param appDeployment deployment details provided by user
     * @param appDeploymentSpec additional information specific to given application deployment
     * @throws NmServiceRequestVerificationException if current deployment environment is not supported by the application
     */
    void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec);

    /**
     * Checks if requested NM service deployment is possible taking into account available resources, currently
     * running services and other constraints.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws NmServiceRequestVerificationException if service deployment is currently not possible
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId);

    /**
     * Executes all initial configuration steps in order to enable further deployment of the NM service.
     *
     * @param deploymentId unique identifier of service deployment
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     * @throws CouldNotPrepareEnvironmentException if any of the environment preparation steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired);

    /**
     * Performs the actual NM service containers deployment.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployNmServiceException if any of the service deployment steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void deployNmService(Identifier deploymentId);

    /**
     * Checks if NM service was successfully deployed and is running.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws ContainerCheckFailedException if some unexpected issue occurred during service deployment status check
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     * @return <code>true</code> if service was deployed successfully
     */
    boolean checkService(Identifier deploymentId);

    /**
     * Retrieves deployed service access details to be presented to the client.
     *
     * @param deploymentId unique identifier of service deployment
     * @return service access details
     * @throws ContainerOrchestratorInternalErrorException if access details are not available for any reason
     */
    AppUiAccessDetails serviceAccessDetails(Identifier deploymentId);

    /**
     * Triggers all the required actions to remove given NM service from the system.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveNmServiceException if any of the service removal steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void removeNmService(Identifier deploymentId);

    /**
     * Triggers all the required actions to restart given NM service.
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRestartNmServiceException if any of the service restart steps failed
     * @throws ContainerOrchestratorInternalErrorException if some internal problem occurred during execution
     */
    void restartNmService(Identifier deploymentId);
}
