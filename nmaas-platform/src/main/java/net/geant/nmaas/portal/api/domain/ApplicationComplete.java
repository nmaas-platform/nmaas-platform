package net.geant.nmaas.portal.api.domain;

public class ApplicationComplete extends Application {

    private AppDeploymentSpec appDeploymentSpec;

	public AppDeploymentSpec getAppDeploymentSpec() {
		return appDeploymentSpec;
	}

	public void setAppDeploymentSpec(AppDeploymentSpec appDeploymentSpec) {
		this.appDeploymentSpec = appDeploymentSpec;
	}
}
