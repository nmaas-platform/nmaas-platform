package net.geant.nmaas.orchestration.api.model;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppDeploymentView {

    private String deploymentId;
    private String deploymentName;
    private String domain;
    private String state;

    public AppDeploymentView() {}

    public AppDeploymentView(String deploymentId, String deploymentName, String domain, String state) {
        this.deploymentId = deploymentId;
        this.deploymentName = deploymentName;
        this.domain = domain;
        this.state = state;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
