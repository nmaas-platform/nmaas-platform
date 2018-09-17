package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

/**
 * Declares methods to be used by the NMaaS Portal to manage NMaaS applications lifecycle.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppLifecycleManager {

    /**
     * Triggers the NMaaS application deployment process which may take some time. This process is executed asynchronously
     * and {@link AppDeploymentState} for this deployment is updated once particular deployment phases
     * are completed. The current {@link AppDeploymentState} may be retrieved from {@link AppDeploymentMonitor}.
     *
     * @param domain unique name of the client domain for which application is deployed
     * @param applicationId unique identifier of the application to be deployed
     * @param deploymentName name of application instance provided by the user
     * @param configFileRepositoryRequired flag which indicates if GitLab instance is required during deployment
     * @return unique identifier of the deployed user application
     */
    Identifier deployApplication(String domain, Identifier applicationId, String deploymentName, boolean configFileRepositoryRequired);

    void redeployApplication(Identifier deploymentId);

    /**
     * Applies custom configuration for the NMaaS application being deployed once provided by the user.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param configuration application configuration provided by the user
     */
    void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException;

    /**
     * Removes deployed application from the system.
     *
     * @param deploymentId unique identifier of the deployed user application
     */
    void removeApplication(Identifier deploymentId) throws InvalidDeploymentIdException;

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
    void updateConfiguration(Identifier deploymentId, AppConfiguration configuration);

    /**
     * Restarts the already running application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @throws InvalidDeploymentIdException if provided deploymentId does not match any deployed application
     */
    void restartApplication(Identifier deploymentId) throws InvalidDeploymentIdException;

}
