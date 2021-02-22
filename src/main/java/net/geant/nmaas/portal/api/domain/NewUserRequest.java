package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@AllArgsConstructor
public class NewUserRequest {
	
	@NotNull
	String username;
	
	Long domainId;

	public NewUserRequest(String username) {
		super();
		this.username = username;
	}
}
