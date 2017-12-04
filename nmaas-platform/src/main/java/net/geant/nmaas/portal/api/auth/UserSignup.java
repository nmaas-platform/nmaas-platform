package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSignup {
	private String username;
	
	private String password;	
	
	private Long domainId;
	
	protected UserSignup() {
		
	}
	
	@JsonCreator
	public UserSignup(@JsonProperty(value="username", required=true) String username, @JsonProperty(value = "password", required=true) String password, @JsonProperty(value="domainId", required = false) Long domainId) {
		this.username = username;
		this.password = password;
		this.domainId = domainId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Long getDomainId() {
		return domainId;
	}
	
	
	
}
