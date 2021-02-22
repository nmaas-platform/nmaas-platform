package net.geant.nmaas.portal.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequest extends UserView {
		
	String password;

	public UserRequest(Long id, String username, String password) {
		super(id, username, false);
		this.password = password;
	}

}
