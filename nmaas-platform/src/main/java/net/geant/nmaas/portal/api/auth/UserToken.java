package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserToken {

	private String token;
	private String refreshToken;
	
	@JsonCreator
	public UserToken(@JsonProperty("token") String token, @JsonProperty("refresh-token") String refreshToken) {
		this.token = token;
		this.refreshToken = refreshToken;
	}
	
	public String getToken() {
		return token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
	
	
}
