package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.validators.ValidUser;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@AllArgsConstructor
@Audited
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ValidUser
public class User implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Include
	@Column(unique = true, nullable = false)
	private String username;
	
	private String password;

	/* Unique string identifying the user received from the IdP during first SAML login */
	private String samlToken;

	@Email
	@Column(unique = true)
	private String email;
	private String firstname;
	private String lastname;
	
	private boolean enabled;

	private boolean termsOfUseAccepted;
	private boolean privacyPolicyAccepted;

	private String selectedLanguage;

	private Long defaultDomain;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "id.user")
	private List<UserRole> roles = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "owner")
	private Set<SSHKeyEntity> sshKeys = new HashSet<>();
	
	public User(String username) {
		this.username = username;
	}
	
	public User(String username, boolean enabled) {
		this(username);
		this.enabled = enabled;
	}
	
	public User(String username, boolean enabled, String password, Domain domain, Role role) {
		this(username, enabled);
		this.password = password;
		this.roles.add(new UserRole(this, domain, role));
	}

	public User(String username, boolean enabled, String password, Domain domain, List<Role> roles) {
		this(username, enabled);
		this.password = password;
		roles.stream().map(r -> new UserRole(this, domain, r)).forEach(this.roles::add);
	}

	public User(String username, boolean enabled, String password, Domain domain, Role role, boolean termsOfUseAccepted) {
		this(username, enabled);
		this.password = password;
		this.termsOfUseAccepted = termsOfUseAccepted;
		this.roles.add(new UserRole(this, domain, role));
	}

    public User(String username, boolean enabled, String password, Domain domain, Role role, boolean termsOfUseAccepted, boolean privacyPolicyAccepted) {
        this(username, enabled);
        this.password = password;
        this.termsOfUseAccepted = termsOfUseAccepted;
        this.privacyPolicyAccepted = privacyPolicyAccepted;
        this.roles.add(new UserRole(this, domain, role));
    }
	
	protected User(Long id, String username, boolean enabled, Domain domain, Role role) {
		this.id = id;
		this.username = username;
		this.enabled = enabled;
		this.getRoles().add(new UserRole(this, domain, role));
	}
	
	protected User(Long id, String username, boolean enabled, String password, List<UserRole> roles) {
		this.id = id;
		this.username = username;
		this.enabled = enabled;
		this.password = password;
		this.roles = roles;
	}

	public void setNewRoles(Set<UserRole> roles) {
		this.roles.addAll(roles);
	}

}
