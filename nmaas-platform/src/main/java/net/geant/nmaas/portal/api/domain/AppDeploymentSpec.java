package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import java.util.List;

public class AppDeploymentSpec {

    private Long id;

    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AppDeploymentEnv> getSupportedDeploymentEnvironments() {
        return supportedDeploymentEnvironments;
    }

    public void setSupportedDeploymentEnvironments(List<AppDeploymentEnv> supportedDeploymentEnvironments) {
        this.supportedDeploymentEnvironments = supportedDeploymentEnvironments;
    }

}
