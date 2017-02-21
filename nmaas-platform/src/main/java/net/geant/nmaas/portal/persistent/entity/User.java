package net.geant.nmaas.portal.persistent.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String username;
	
	@NotNull
	private String password;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private List<UserRole> roles = new ArrayList<UserRole>();

	protected User() {
	}
	
	public User(String username, String password, Role role) {
		this.username = username;
		this.password = password;
		this.roles.add(new UserRole(this, role));
	}

	public User(String username, String password, List<Role> roles) {
		this.username = username;
		this.password = password;
		for (Role role : roles) {
			this.roles.add(new UserRole(this, role));
		}	
	}
	
	protected User(Long id, String username, String password, List<UserRole> roles) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.roles = roles;
	}


	public Long getId() {
		return id;
	}


	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public List<UserRole> getRoles() {
		return roles;
	}	
	
}
