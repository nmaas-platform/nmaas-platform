package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Audited
public class UserRole implements Serializable {

	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@Setter(AccessLevel.PROTECTED)
	@Getter
	@Audited
	@Embeddable
	public static class Id implements Serializable {

		@OneToOne
		@NotNull
		private User user;
		
		@OneToOne
		@NotNull
		private Domain domain;

		@Enumerated(EnumType.STRING)
		@NotNull
		private Role role;

	}
	
	@EmbeddedId
	Id id = new Id();
	
	public UserRole(User user, Domain domain, Role role) {
		if(user == null)
			throw new IllegalStateException("User is null");
		if(domain == null)
			throw new IllegalStateException("Domain is null");
		if(role == null)
			throw new IllegalStateException("Role is null");
		id = new Id(user, domain, role);
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	@Transient
	public Role getRole() {
		return id.getRole();
	}
	
	@Transient
	public Domain getDomain() {
		return id.getDomain();
	}
	
	@Transient
	public User getUser() {
		return id.getUser();
	}
	
	@Transient
	public String getAuthority() {
		return id.getDomain().getId() + ":" + id.getRole().authority();
	}

	@Transient
	public String getRoleAsString() {
		return id.getRole() + " in " + id.getDomain().getCodename();
	}
	
}
