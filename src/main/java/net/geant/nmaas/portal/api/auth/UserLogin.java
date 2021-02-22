package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UserLogin {
	
	private String username;
	private String password;
	
	@JsonCreator
	public UserLogin(@JsonProperty("username") String username, @JsonProperty("password") String password) {
		this.username = username;
		this.password = password;
	}
}
