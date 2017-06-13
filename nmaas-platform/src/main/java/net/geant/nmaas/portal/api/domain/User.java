package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import net.geant.nmaas.portal.persistent.entity.Role;

public class User {

	Long id;
	
	@NotNull
	String username;
	
	List<Role> roles = new ArrayList<Role>();
	
	public User() {
		// TODO Auto-generated constructor stub
	}
	
	public User(Long id, String username, List<Role> roles) {
		super();
		this.id = id;
		this.username = username;
		this.roles = roles;
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	
}
