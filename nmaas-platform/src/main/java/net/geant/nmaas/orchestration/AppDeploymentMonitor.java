package net.geant.nmaas.orchestration;

/**
 * Declares a method to retrieve the current state of application being deployed using {@link AppLifecycleManager} API
 * and another one to retrieve user access details to deployed application once available.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentMonitor {

    /**
     * Retrieves the current state of processed application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return current state of the application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException;

    /**
     * Retrieves user access details to deployed application. This information becomes available once the application
     * deployment is fully completed, meaning that application deployment is verified.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @return application user access details
     * @throws InvalidAppStateException if application deployment state {@link AppLifecycleState} is not the expected one
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any processed application
     */
    AppUiAccessDetails userAccessDetails(Identifier deploymentId) throws InvalidAppStateException, InvalidDeploymentIdException;

}
