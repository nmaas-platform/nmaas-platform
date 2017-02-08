package net.geant.nmaas.deploymentorchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentOrchestrator {

    /**
     * Triggers the application deployment process which may take some time. This process is executed asynchronously
     * and {@link AppDeploymentState} for this deployment is updated once particular deployment phases
     * are completed.
     *
     * @param clientId unique identifier of the client
     * @param applicationId unique identifier of the application to be deployed
     * @return unique identifier of the deployed user application
     */
    String deployApplication(String clientId, String applicationId);

    void applyConfiguration(String deploymentId, AppConfiguration configuration);


    void removeApplication(String deploymentId);

    /**
     * Updates already deployed user application to the latest or provided by client version available in the NMaaS Portal.
     * This most probably requires application container redeployment.
     * Application configuration and persistent data must be restored.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param applicationId unique identifier of the desired version or originally deployed application
     */
    void updateApplication(String deploymentId, String applicationId);

    /**
     * Updates the configuration of already deployed user application.
     *
     * @param deploymentId unique identifier of the deployed user application
     * @param configuration updated configuration provided by client
     */
    void updateConfiguration(String deploymentId, AppConfiguration configuration);

}
