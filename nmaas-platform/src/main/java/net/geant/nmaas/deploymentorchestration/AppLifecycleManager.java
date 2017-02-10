package net.geant.nmaas.deploymentorchestration;

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
     * @param clientId unique identifier of the client
     * @param applicationId unique identifier of the application to be deployed
     * @return unique identifier of the deployed user application
     */
    Identifier deployApplication(Identifier clientId, Identifier applicationId);

    /**
     * Applies custom configuration for the NMaaS application being deployed once provided by the client.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param configuration application user configuration
     */
    void applyConfiguration(Identifier deploymentId, AppConfiguration configuration);

    /**
     * Removes deployed application from the system.
     *
     * @param deploymentId unique identifier of the deployed user application
     */
    void removeApplication(Identifier deploymentId);

    /**
     * Updates already deployed user application to the latest or provided by client version available in the NMaaS Portal.
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
     * @param configuration updated configuration provided by client
     */
    void updateConfiguration(Identifier deploymentId, AppConfiguration configuration);

}
