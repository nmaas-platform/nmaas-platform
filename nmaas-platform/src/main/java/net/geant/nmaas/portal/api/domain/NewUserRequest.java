package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

public class NewUserRequest {
	
	@NotNull
	String username;
	
	Long domainId;
	
	protected NewUserRequest() {
		super();
	}

	public NewUserRequest(String username) {
		super();
		this.username = username;
	}
	
	public NewUserRequest(String username, Long domainId) {
		super();
		this.username = username;
		this.domainId = domainId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	
	
}
