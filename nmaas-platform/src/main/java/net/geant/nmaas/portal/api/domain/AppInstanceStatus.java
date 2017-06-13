package net.geant.nmaas.portal.api.domain;

public class AppInstanceStatus {
	Long appInstanceId;
	AppInstanceState state;
	String details;
	public Long getAppInstanceId() {
		return appInstanceId;
	}
	public void setAppInstanceId(Long appInstanceId) {
		this.appInstanceId = appInstanceId;
	}
	public AppInstanceState getState() {
		return state;
	}
	public void setState(AppInstanceState state) {
		this.state = state;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
}
