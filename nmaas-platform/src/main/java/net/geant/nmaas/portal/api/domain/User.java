package net.geant.nmaas.portal.api.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends UserBase {
	
	protected String firstname;
	protected String lastname;
	protected String email;
	protected boolean ssoUser;
	
	protected Set<UserRole> roles = new HashSet<>();
	
	protected User() {
		super();
	}
	
	public User(Long id, String username) {
		super(id, username);
	}

	@Builder
	public User(Long id, String username, String firstname, String lastname, String email, Set<UserRole> roles, boolean ssoUser) {
		this(id, username);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.roles = roles;
		this.ssoUser = ssoUser;
	}
	
}
