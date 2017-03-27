package net.geant.nmaas.portal.api.domain;

public class ApiResponse {
	boolean success;
	
	public ApiResponse(boolean success) {
		super();
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}	
}
