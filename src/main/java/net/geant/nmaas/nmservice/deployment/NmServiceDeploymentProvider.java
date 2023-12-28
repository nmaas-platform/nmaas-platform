package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveNmServiceAccessDetailsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotUpgradeKubernetesServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppComponentDetails;
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
     * Creates new object representing the NM service deployment and verifies if the request can be executed.
     *
     * @param deploymentId unique identifier of service deployment
     * @param appDeployment application deployment details provided by user
     * @param appDeploymentSpec additional information specific to given application deployment
     * @throws NmServiceRequestVerificationException if service can't be deployed or some input parameters are missing
     */
    void verifyRequest(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec);

    /**
     * Coordinates deployment environment preparation (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @param configFileRepositoryRequired indicates if GitLab instance is required during deployment
     * @throws CouldNotPrepareEnvironmentException if environment couldn't be prepared for some reason
     */
    void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired);

    /**
     * Coordinates service deployment (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployNmServiceException if NM service couldn't be deployed for some reason
     */
    void deployService(Identifier deploymentId);

    /**
     * Coordinates service deployment verification (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotVerifyNmServiceException if NM service deployment verification failed
     */
    void verifyService(Identifier deploymentId);

    /**
     * Retrieves deployed service access details to be presented to the client.
     *
     * @param deploymentId unique identifier of service deployment
     * @return service access details
     * @throws CouldNotRetrieveNmServiceAccessDetailsException if access details are not available for any reason
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
     * @throws CouldNotRemoveNmServiceException if NM service couldn't be removed for some reason
     */
    void removeService(Identifier deploymentId);

    /**
     * Coordinates service restart (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRestartNmServiceException if NM service couldn't be restarted for some reason
     */
    void restartService(Identifier deploymentId);

    /**
     * Coordinates service upgrade to specified version (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @param mode application upgrade mode
     * @param targetApplicationId target application identifier
     * @param kubernetesTemplate Helm chart information of the desired application version
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
}
