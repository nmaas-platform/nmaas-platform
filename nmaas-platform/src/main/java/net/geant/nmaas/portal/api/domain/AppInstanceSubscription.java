package net.geant.nmaas.portal.api.domain;

public class AppInstanceSubscription {
	private Long applicationId;
	
	private String name;

	public AppInstanceSubscription() {
		super();
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
