package net.geant.nmaas.portal.api.domain;

import java.util.List;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequest extends User {
		
	String password;

	public UserRequest(Long id, String username, String password) {
		super(id, username);
		this.password = password;
	}	

	
}
