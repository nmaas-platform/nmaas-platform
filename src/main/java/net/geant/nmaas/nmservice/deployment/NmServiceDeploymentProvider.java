package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveServiceAccessDetailsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPauseServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotResumeServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotUpgradeKubernetesServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.ServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.AppComponentLogs;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.AppUpgradeMode;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;

import java.util.List;
import java.util.Map;

/**
 * Defines a set of methods to manage service deployment lifecycle.
 */
public interface NmServiceDeploymentProvider {

    /**
     * Creates new object representing the service deployment and verifies if the request can be executed.
     *
     * @param deploymentId      unique identifier of service deployment
     * @param appDeployment     application deployment details provided by user
     * @param appDeploymentSpec additional information specific to given application deployment
     * @throws ServiceRequestVerificationException if service can't be deployed or some input parameters are missing
     */
    void verifyRequest(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec);

    /**
     * Coordinates deployment environment preparation (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId                 unique identifier of service deployment
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     * @throws CouldNotPrepareEnvironmentException if environment couldn't be prepared for some reason
     */
    void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired);

    /**
     * Coordinates service deployment (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployServiceException if service couldn't be deployed for some reason
     */
    void deployService(Identifier deploymentId);

    /**
     * Coordinates service deployment verification (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotVerifyServiceException if service deployment verification failed
     */
    void verifyService(Identifier deploymentId);

    /**
     * Retrieves deployed service access details to be presented to the client.
     *
     * @param deploymentId unique identifier of service deployment
     * @return service access details
     * @throws CouldNotRetrieveServiceAccessDetailsException if access details are not available for any reason
     */
    AppUiAccessDetails serviceAccessDetails(Identifier deploymentId);

    /**
     * Retrieves various parameters of the deployed service.
     *
     * @param deploymentId unique identifier of service deployment
     * @return map of deployment parameters with their key and value
     */
    Map<String, String> serviceDeployParameters(Identifier deploymentId);

    /**
     * Coordinates service removal (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveServiceException if service couldn't be removed for some reason
     */
    void removeService(Identifier deploymentId);

    /**
     * Coordinates service restart (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRestartServiceException if service couldn't be restarted for some reason
     */
    void restartService(Identifier deploymentId);

    /**
     * Coordinates service upgrade to specified version (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId        unique identifier of service deployment
     * @param mode                application upgrade mode
     * @param targetApplicationId target application identifier
     * @param kubernetesTemplate  Helm chart information of the desired application version
     * @throws CouldNotUpgradeKubernetesServiceException if service couldn't be upgraded for some reason
     */
    void upgradeKubernetesService(Identifier deploymentId, AppUpgradeMode mode, Identifier targetApplicationId, KubernetesTemplate kubernetesTemplate);

    /**
     * Retrieves components of the deployed service.
     *
     * @param deploymentId unique identifier of service deployment
     * @return list of {@link AppComponentDetails} objects
     */
    List<AppComponentDetails> serviceComponents(Identifier deploymentId);

    /**
     * Retrieves logs from given service component.
     *
     * @param deploymentId            unique identifier of service deployment
     * @param serviceComponentName    name of service component from which logs should be retrieved
     * @param serviceSubComponentName name of service subcomponent (added if required)
     * @param limit                   number of log lines to be returned (optional)
     * @return {@link AppComponentLogs} object containing application logs
     */
    AppComponentLogs serviceComponentLogs(Identifier deploymentId, String serviceComponentName, String serviceSubComponentName, int limit);

    /**
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotPauseServiceException if service couldn't be paused for some reason
     */
    void pauseService(Identifier deploymentId);

    /**
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotResumeServiceException if service couldn't be resumed for some reason
     */
    void resumeService(Identifier deploymentId);

}
