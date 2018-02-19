package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import net.geant.nmaas.portal.persistent.entity.Role;

public class User {

	Long id;
	
	@NotNull
	String username;
	
	Set<UserRole> roles = new HashSet<UserRole>();
	
	public User() {
		// TODO Auto-generated constructor stub
	}
	
	public User(Long id, String username) {
		this.id = id;
		this.username = username;		
	}
	
	public User(Long id, String username, Set<UserRole> roles) {
		this(id, username);
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

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}
	
	
}
