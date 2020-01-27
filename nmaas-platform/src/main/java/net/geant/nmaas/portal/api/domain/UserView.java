package net.geant.nmaas.portal.api.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserView extends UserBase implements Serializable {
	
	protected String firstname;
	protected String lastname;
	protected String email;
	protected boolean ssoUser;
	protected String selectedLanguage;

	protected OffsetDateTime lastSuccessfulLoginDate;
	protected OffsetDateTime firstLoginDate;
	
	protected Set<UserRoleView> roles = new HashSet<>();
	
	protected UserView() {
		super();
	}
	
	public UserView(Long id, String username) {
		super(id, username);
	}

	@Builder
	public UserView(Long id, String username, String firstname, String lastname, String email, boolean enabled, Set<UserRoleView> roles, boolean ssoUser, String selectedLanguage) {
		this(id, username);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.enabled = enabled;
		this.roles = roles;
		this.ssoUser = ssoUser;
		this.selectedLanguage = selectedLanguage;
	}
	
}
