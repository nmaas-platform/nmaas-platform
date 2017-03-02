package net.geant.nmaas.portal.api.domain;

import java.util.Date;

public class AppInstance {

	private Long id;
	
	private Long applicationId;
	
	private String name;
	
	private Date createdAt;
	
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	
	
}
