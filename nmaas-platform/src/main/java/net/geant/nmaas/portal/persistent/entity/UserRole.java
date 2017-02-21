package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class UserRole {

	@Embeddable
	public static class Id implements Serializable {
		@OneToOne
		@NotNull
		protected User user;
		
		@Enumerated(EnumType.STRING)
		@NotNull
		protected Role role;
		
		public Id() {
		}

		public Id(User user, Role role) {
			this.user = user;
			this.role = role;
		}		
	}
	
	@EmbeddedId
	Id id = new Id();
	
	protected UserRole() {
		
	}
	
	public UserRole(User user, Role role) {
		if(user == null)
			throw new IllegalStateException("User is null");
		id = new Id(user, role);
		this.role = role;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, insertable = false, updatable = false)
	protected Role role;
	
	public Role getRole() {
		return role;
	}
	
	public String getAuthority() {
		return role.authority();
	}
	
}
