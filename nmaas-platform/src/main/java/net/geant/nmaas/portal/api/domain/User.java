package net.geant.nmaas.portal.api.domain;

import java.util.HashSet;
import java.util.Set;

public class User extends UserBase {
	
	protected String firstname;
	protected String lastname;
	protected String email;
	
	protected Set<UserRole> roles = new HashSet<UserRole>();
	
	protected User() {
		super();
	}
	
	public User(Long id, String username) {
		super(id, username);
	}
	
	public User(Long id, String username, Set<UserRole> roles) {
		this(id, username);
		this.roles = roles;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
