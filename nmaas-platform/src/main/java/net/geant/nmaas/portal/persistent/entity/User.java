package net.geant.nmaas.portal.persistent.entity;

import javax.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.geant.nmaas.portal.persistent.entity.validators.ValidUser;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ValidUser
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Include
	@Column(unique = true, nullable = false)
	private String username;
	
	private String password;

	private String samlToken;

	@Email
	private String email;
	private String firstname;
	private String lastname;
	
	private boolean enabled;

	private boolean termsOfUseAccepted;
	private boolean privacyPolicyAccepted;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, mappedBy="id.user")
	private List<UserRole> roles = new ArrayList<>();
	
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
		for (Role role : roles) {
			this.roles.add(new UserRole(this, domain, role));
		}	
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

	public void clearRoles() {
		this.roles.clear();
	}

	public void setNewRoles(Set<UserRole> roles) {
		if(roles.stream().anyMatch(role-> role.getRole() == Role.ROLE_SYSTEM_COMPONENT))
			throw new IllegalStateException("This role cannot be assigned");
		this.roles.addAll(roles);
	}

}
