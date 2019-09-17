package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

/**
 * Declares methods to be used by the NMaaS Portal to manage NMaaS applications lifecycle.
 */
public interface AppLifecycleManager {

    /**
     * Triggers the NMaaS application deployment process which may take some time. This process is executed asynchronously
     * and {@link AppDeploymentState} for this deployment is updated once particular deployment phases
     * are completed. The current {@link AppDeploymentState} may be retrieved from {@link AppDeploymentMonitor}.
     *
     * @param appDeployment App deployment
     * @return unique identifier of the deployed user application
     */
    Identifier deployApplication(AppDeployment appDeployment);

    /**
     * Triggers the NMaaS application redeployment process which may take some time.This process is executed asynchronously
     * and {@link AppDeploymentState} for this deployment is updated once particular deployment phases
     * are completed. The current {@link AppDeploymentState} may be retrieved from {@link AppDeploymentMonitor}.
     * @param deploymentId unique identifier of the user application
     */
    void redeployApplication(Identifier deploymentId);

    /**
     * Applies custom configuration for the NMaaS application being deployed once provided by the user.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param configuration configuration provided by user in application deployment wizard
     */
    void applyConfiguration(Identifier deploymentId, AppConfigurationView configuration) throws Throwable;

    /**
     * Removes deployed application from the system.
     *
     * @param deploymentId unique identifier of the deployed user application
     */
    void removeApplication(Identifier deploymentId);

    /**
     * Updates already deployed user application to the latest or provided by user version available in the NMaaS Portal.
     * This most probably requires application container redeployment.
     * Application configuration and persistent data must be restored.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param applicationId unique identifier of the desired version or originally deployed application
     */
    void updateApplication(Identifier deploymentId, Identifier applicationId);

    /**
     * Updates the configuration of already deployed user application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param configuration updated application configuration provided by the user
     */
    void updateConfiguration(Identifier deploymentId, AppConfigurationView configuration);

    /**
     * Restarts the already running application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any deployed application
     */
    void restartApplication(Identifier deploymentId);


    /**
     * Removes application, if it fails on any stage of deployment. Additionally, it rollback successful stages.
     *
     * @param deploymentId unique identifier of the deployed user application
     */
    void removeFailedApplication(Identifier deploymentId);
}
