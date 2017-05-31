package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.entities.AppDeployment;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppDeploymentView {

    private String deploymentId;
    private String clientId;
    private String applicationId;
    private String state;

    public AppDeploymentView() {}

    public AppDeploymentView(AppDeployment appDeployment) {
        this.deploymentId = appDeployment.getDeploymentId().value();
        this.clientId = appDeployment.getClientId().value();
        this.applicationId = appDeployment.getApplicationId().value();
        this.state = appDeployment.getState().name();
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
