package net.geant.nmaas.portal.api.domain;

import java.util.Date;

public class AppInstance {

	private Long id;
	
	private Long applicationId;
	
	private String name;
	
	private Long createdAt;
	
	private User owner;
	
	private AppInstanceState state;
	
	public AppInstance() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public AppInstanceState getState() {
		return state;
	}

	public void setState(AppInstanceState state) {
		this.state = state;
	}


	
	
}
