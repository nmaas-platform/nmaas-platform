package net.geant.nmaas.orchestration.api.model;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppDeploymentView {

    private String deploymentId;
    private String domain;
    private String applicationId;
    private String state;

    public AppDeploymentView() {}

    public AppDeploymentView(String deploymentId, String domain, String applicationId, String state) {
        this.deploymentId = deploymentId;
        this.domain = domain;
        this.applicationId = applicationId;
        this.state = state;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
