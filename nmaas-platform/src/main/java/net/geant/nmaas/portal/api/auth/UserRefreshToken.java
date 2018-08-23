package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UserRefreshToken {

	private String refreshToken;
	
	@JsonCreator
	public UserRefreshToken(@JsonProperty(value = "refresh-token", required=true) String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
}
