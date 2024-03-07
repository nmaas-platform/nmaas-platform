package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

import java.util.List;
import java.util.Map;

/**
 * Declares a method to retrieve the current state of application being deployed using {@link AppLifecycleManager} API
 * and another one to retrieve user access details to deployed application once available.
 */
public interface AppDeploymentMonitor {

    /**
     * Retrieves the current state of deployed application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return current state of the application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppLifecycleState state(Identifier deploymentId);

    /**
     * Retrieves the previous state of deployed application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return previous state of the application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppLifecycleState previousState(Identifier deploymentId);

    /**
     * Retrieves information on all deployments in the system.
     *
     * @return identifiers of all deployments
     */
    List<AppDeployment> allDeployments();

    /**
     * Retrieves user access details to deployed application. This information becomes available once the application
     * deployment is fully completed, meaning that application deployment is verified.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return application user access details
     * @throws InvalidAppStateException if application deployment state {@link AppLifecycleState} is not the expected one
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppUiAccessDetails userAccessDetails(Identifier deploymentId);

    /**
     * Retrieves map of application deployment parameters that can be further exposed to the user.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return map of deployment parameters with their key and value
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    Map<String, String> appDeploymentParameters(Identifier deploymentId);

    /**
     * Retrieves the URL that should be used in order to clone the Git repository that contains configuration files
     * of the deployed application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return a class containing the URL to be used for cloning the repository with SSH
     */
    AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId);

    /**
     * Retrieves information about application deployment state transitions.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return all state changes of the application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    List<AppDeploymentHistoryView> appDeploymentHistory(Identifier deploymentId);

    /**
     * Retrieves list of application deployment components.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return list of deployment components
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    List<AppComponentDetails> appComponents(Identifier deploymentId);

    /**
     * Retrieves logs from specified application deployment component.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param appComponentName name of the component which logs should be collected
     * @param appSubComponentName name of a subcomponent (added if required)
     * @return objects representing logs from application component
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppComponentLogs appComponentLogs(Identifier deploymentId, String appComponentName, String appSubComponentName);
}
