package net.geant.nmaas.portal.api.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
	
}
