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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Id other = (Id) obj;
			if (role != other.role)
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
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
