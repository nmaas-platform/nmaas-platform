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
	protected Long defaultDomain;

	protected OffsetDateTime lastSuccessfulLoginDate;
	protected OffsetDateTime firstLoginDate;
	
	private Set<UserRoleView> roles = new HashSet<>();

	private Set<SSHKeyView> sshKeys = new HashSet<>();
	
	protected UserView() {
		super();
	}
	
	public UserView(Long id, String username, boolean enabled) {
		super(id, username, enabled);
	}

	@Builder
	public UserView(Long id, String username, String firstname, String lastname, String email, boolean enabled, Set<UserRoleView> roles, boolean ssoUser, String selectedLanguage, Set<SSHKeyView> sshKeys, Long defaultDomain) {
		this(id, username, enabled);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.roles = roles;
		this.ssoUser = ssoUser;
		this.selectedLanguage = selectedLanguage;
		this.sshKeys = sshKeys;
		this.defaultDomain = defaultDomain;
	}
	
}
