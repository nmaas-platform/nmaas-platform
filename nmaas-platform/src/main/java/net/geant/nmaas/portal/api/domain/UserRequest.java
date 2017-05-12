package net.geant.nmaas.portal.api.domain;

import java.util.List;


import net.geant.nmaas.portal.persistent.entity.Role;

public class UserRequest extends User {
		
	String password;
	
	public UserRequest(Long id, String username, String password, List<Role> roles) {
		super(id, username, roles);
		this.password = password;
	}	
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
}
