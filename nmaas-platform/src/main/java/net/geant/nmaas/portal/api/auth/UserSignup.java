package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSignup {
	private String username;
	
	private String password;

	protected UserSignup() {
		
	}
	
	@JsonCreator
	public UserSignup(@JsonProperty(value="username", required=true) String username, @JsonProperty(value = "password", required=true) String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	
}
